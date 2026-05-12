<script setup lang="ts">
import { onMounted, ref, computed } from "vue";
import {
    fetchStore,
    installBundle,
    validateBundle,
    type StoreEntry,
} from "../api";
import { useAppStore } from "../stores/appStore";

const app = useAppStore();
const entries = ref<StoreEntry[]>([]);
const search = ref("");
const sourceFilter = ref<"ALL" | "OFFICIAL" | "COMMUNITY">("ALL");
const typeFilter = ref<"ALL" | "PLUGIN" | "BUNDLE">("ALL");
const selectedEntry = ref<StoreEntry | null>(null);
const installing = ref<string | null>(null);
const validating = ref<string | null>(null);
const validationResult = ref<{ valid: boolean; errors: string[] } | null>(null);
const showConfirm = ref(false);
const confirmChecked = ref(false);

onMounted(async () => {
    try {
        entries.value = await fetchStore();
    } catch (e) {
        app.setError(e instanceof Error ? e.message : "Failed to load store");
    }
});

const filtered = computed(() => {
    let list = entries.value;
    if (sourceFilter.value !== "ALL") {
        list = list.filter(
            (e) => e.source.toUpperCase() === sourceFilter.value,
        );
    }
    if (typeFilter.value !== "ALL") {
        list = list.filter((e) => e.type === typeFilter.value);
    }
    const q = search.value.trim().toLowerCase();
    if (q) {
        list = list.filter(
            (e) =>
                e.name.toLowerCase().includes(q) ||
                (e.description?.toLowerCase().includes(q) ?? false) ||
                (e.tags?.some((t) => t.toLowerCase().includes(q)) ?? false),
        );
    }
    return list;
});

const allTags = computed(() => {
    const set = new Set<string>();
    entries.value.forEach((e) => e.tags?.forEach((t) => set.add(t)));
    return Array.from(set).sort();
});

const tagFilter = ref<string[]>([]);

const filteredByTag = computed(() => {
    if (!tagFilter.value.length) return filtered.value;
    return filtered.value.filter((e) =>
        e.tags?.some((t) => tagFilter.value.includes(t)),
    );
});

function toggleTag(tag: string) {
    if (tagFilter.value.includes(tag)) {
        tagFilter.value = tagFilter.value.filter((t) => t !== tag);
    } else {
        tagFilter.value = [...tagFilter.value, tag];
    }
}

function openDetail(entry: StoreEntry) {
    selectedEntry.value = entry;
    validationResult.value = null;
    showConfirm.value = false;
    confirmChecked.value = false;
}

function closeDetail() {
    selectedEntry.value = null;
    validationResult.value = null;
    showConfirm.value = false;
    confirmChecked.value = false;
}

async function doValidate(id: string) {
    validating.value = id;
    try {
        validationResult.value = await validateBundle(id);
    } catch (e) {
        app.setError(e instanceof Error ? e.message : "Validation failed");
    } finally {
        validating.value = null;
    }
}

async function doInstallBundle(id: string) {
    const entry = entries.value.find((e) => e.id === id);
    if (entry?.source.toUpperCase() === "COMMUNITY" && !showConfirm.value) {
        showConfirm.value = true;
        return;
    }
    if (showConfirm.value && !confirmChecked.value) {
        return;
    }
    installing.value = id;
    try {
        const result = await installBundle(id);
        if (!result.success) {
            app.setError("Bundle install failed: " + result.errors.join(", "));
        } else {
            app.setError("");
        }
    } catch (e) {
        app.setError(e instanceof Error ? e.message : "Bundle install failed");
    } finally {
        installing.value = null;
        showConfirm.value = false;
        confirmChecked.value = false;
    }
}
</script>

<template>
    <header>
        <p>Plugin Runtime</p>
        <h1>Store</h1>
    </header>
    <p v-if="app.error" class="error">{{ app.error }}</p>

    <section class="panel store-panel">
        <div class="store-toolbar">
            <input
                v-model="search"
                class="store-search"
                placeholder="Search plugins, bundles, tags..."
            />
            <div class="tab-bar">
                <button
                    v-for="s in ['ALL', 'OFFICIAL', 'COMMUNITY'] as const"
                    :key="s"
                    :class="{ active: sourceFilter === s }"
                    @click="sourceFilter = s"
                >
                    {{
                        s === "ALL"
                            ? "All Sources"
                            : s.charAt(0) + s.slice(1).toLowerCase()
                    }}
                </button>
            </div>
            <div class="tab-bar">
                <button
                    v-for="t in ['ALL', 'PLUGIN', 'BUNDLE'] as const"
                    :key="t"
                    :class="{ active: typeFilter === t }"
                    @click="typeFilter = t"
                >
                    {{
                        t === "ALL"
                            ? "All Types"
                            : t.charAt(0) + t.slice(1).toLowerCase() + "s"
                    }}
                </button>
            </div>
        </div>

        <div v-if="allTags.length" class="tag-filter-bar">
            <span class="tag-filter-label">Tags:</span>
            <button
                v-for="tag in allTags"
                :key="tag"
                class="tag-chip"
                :class="{ active: tagFilter.includes(tag) }"
                @click="toggleTag(tag)"
            >
                {{ tag }}
            </button>
        </div>

        <p v-if="!filteredByTag.length" class="store-empty">
            No entries match your filters.
        </p>

        <div v-else class="store-grid">
            <div
                v-for="entry in filteredByTag"
                :key="entry.id"
                class="store-card"
                @click="openDetail(entry)"
            >
                <div class="store-card-header">
                    <span class="store-card-name">{{ entry.name }}</span>
                    <span
                        class="state-badge"
                        :class="entry.source.toLowerCase()"
                        >{{ entry.source }}</span
                    >
                </div>
                <div class="store-card-meta">
                    <span>{{ entry.type }}</span>
                    <span>v{{ entry.version }}</span>
                    <span v-if="entry.author">by {{ entry.author }}</span>
                </div>
                <p v-if="entry.description" class="store-card-desc">
                    {{ entry.description }}
                </p>
                <div v-if="entry.tags?.length" class="store-card-tags">
                    <span
                        v-for="tag in entry.tags.slice(0, 4)"
                        :key="tag"
                        class="tag-chip"
                        >{{ tag }}</span
                    >
                    <span v-if="entry.tags.length > 4" class="tag-chip"
                        >+{{ entry.tags.length - 4 }}</span
                    >
                </div>
            </div>
        </div>
    </section>

    <!-- Detail Modal -->
    <div v-if="selectedEntry" class="modal-overlay" @click.self="closeDetail">
        <div class="modal-card">
            <div class="modal-header">
                <div>
                    <h2>{{ selectedEntry.name }}</h2>
                    <div class="modal-meta">
                        <span
                            class="state-badge"
                            :class="selectedEntry.source.toLowerCase()"
                            >{{ selectedEntry.source }}</span
                        >
                        <span class="state-badge unknown">{{
                            selectedEntry.type
                        }}</span>
                        <span>v{{ selectedEntry.version }}</span>
                        <span v-if="selectedEntry.author"
                            >by {{ selectedEntry.author }}</span
                        >
                        <span v-if="selectedEntry.license">{{
                            selectedEntry.license
                        }}</span>
                    </div>
                </div>
                <button class="modal-close" @click="closeDetail">
                    &times;
                </button>
            </div>

            <p v-if="selectedEntry.description" class="modal-desc">
                {{ selectedEntry.description }}
            </p>

            <div v-if="selectedEntry.tags?.length" class="modal-tags">
                <span
                    v-for="tag in selectedEntry.tags"
                    :key="tag"
                    class="tag-chip"
                    >{{ tag }}</span
                >
            </div>

            <div v-if="selectedEntry.minSynapse" class="modal-min-version">
                Requires Synapse &ge; {{ selectedEntry.minSynapse }}
            </div>

            <div v-if="selectedEntry.type === 'BUNDLE'" class="modal-actions">
                <button
                    :disabled="validating === selectedEntry.id"
                    @click="doValidate(selectedEntry.id)"
                >
                    {{
                        validating === selectedEntry.id
                            ? "Validating..."
                            : "Validate Bundle"
                    }}
                </button>
                <button
                    :disabled="installing === selectedEntry.id"
                    @click="doInstallBundle(selectedEntry.id)"
                >
                    {{
                        installing === selectedEntry.id
                            ? "Installing..."
                            : "Install Bundle"
                    }}
                </button>
            </div>

            <div v-if="showConfirm" class="modal-confirm">
                <p>This bundle is from a community source. Install anyway?</p>
                <label
                    style="
                        display: flex;
                        gap: 8px;
                        align-items: center;
                        color: #aab4bf;
                        font-size: 0.85rem;
                    "
                >
                    <input type="checkbox" v-model="confirmChecked" />
                    I confirm installing this community/unverified bundle
                </label>
                <button @click="doInstallBundle(selectedEntry.id)">
                    Confirm Install
                </button>
            </div>

            <div v-if="validationResult" class="modal-validation">
                <p :class="validationResult.valid ? 'valid' : 'error'">
                    {{
                        validationResult.valid
                            ? "Bundle is valid"
                            : "Bundle validation failed"
                    }}
                </p>
                <ul v-if="validationResult.errors.length">
                    <li
                        v-for="err in validationResult.errors"
                        :key="err"
                        class="error"
                    >
                        {{ err }}
                    </li>
                </ul>
            </div>
        </div>
    </div>
</template>
