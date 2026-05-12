package dev.synapse.plugins.loader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Semver version constraint parser and evaluator.
 *
 * <p>Supported formats:
 * <ul>
 *   <li>{@code *} — any version</li>
 *   <li>{@code 1.0.0} — exact match</li>
 *   <li>{@code >=1.0.0} — greater than or equal</li>
 *   <li>{@code >1.0.0} — greater than</li>
 *   <li>{@code <=1.0.0} — less than or equal</li>
 *   <li>{@code <1.0.0} — less than</li>
 *   <li>{@code ^1.0.0} — compatible (same major, >= minor.patch)</li>
 *   <li>{@code ~1.0.0} — approximately (same major.minor, >= patch)</li>
 * </ul>
 */
public final class VersionConstraint {

    private static final Pattern CONSTRAINT_PATTERN = Pattern.compile(
        "^(>=|<=|>|<|\\^|~)?(\\d+)\\.(\\d+)\\.(\\d+)(?:-(.+))?$"
    );

    private final String raw;
    private final Operator operator;
    private final int major;
    private final int minor;
    private final int patch;
    private final String prerelease;

    private enum Operator {
        ANY, EQ, GTE, GT, LTE, LT, CARET, TILDE
    }

    private VersionConstraint(String raw, Operator operator, int major, int minor, int patch, String prerelease) {
        this.raw = raw;
        this.operator = operator;
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.prerelease = prerelease;
    }

    /**
     * Parses a version constraint string.
     *
     * @throws IllegalArgumentException if the format is invalid
     */
    public static VersionConstraint parse(String constraint) {
        if (constraint == null || constraint.equals("*")) {
            return new VersionConstraint("*", Operator.ANY, 0, 0, 0, null);
        }

        Matcher m = CONSTRAINT_PATTERN.matcher(constraint);
        if (!m.matches()) {
            throw new IllegalArgumentException("Invalid version constraint: " + constraint);
        }

        String opStr = m.group(1);
        Operator op = switch (opStr != null ? opStr : "") {
            case ">=" -> Operator.GTE;
            case ">" -> Operator.GT;
            case "<=" -> Operator.LTE;
            case "<" -> Operator.LT;
            case "^" -> Operator.CARET;
            case "~" -> Operator.TILDE;
            default -> Operator.EQ;
        };

        int major = Integer.parseInt(m.group(2));
        int minor = Integer.parseInt(m.group(3));
        int patch = Integer.parseInt(m.group(4));
        String prerelease = m.group(5);

        return new VersionConstraint(constraint, op, major, minor, patch, prerelease);
    }

    /**
     * Checks if the given version satisfies this constraint.
     */
    public boolean satisfies(String version) {
        if (operator == Operator.ANY) {
            return true;
        }

        Semver v = Semver.parse(version);
        Semver target = new Semver(major, minor, patch, prerelease);

        int cmp = v.compareTo(target);

        return switch (operator) {
            case EQ -> cmp == 0;
            case GTE -> cmp >= 0;
            case GT -> cmp > 0;
            case LTE -> cmp <= 0;
            case LT -> cmp < 0;
            case CARET -> v.major == major && cmp >= 0;
            case TILDE -> v.major == major && v.minor == minor && cmp >= 0;
            default -> false;
        };
    }

    /** Returns true if this constraint represents a newer version requirement. */
    public boolean isNewerThan(String version) {
        if (operator == Operator.ANY) {
            return false;
        }
        Semver v = Semver.parse(version);
        Semver target = new Semver(major, minor, patch, prerelease);
        return target.compareTo(v) > 0;
    }

    @Override
    public String toString() {
        return raw;
    }

    /** Internal semver representation for comparison. */
    private record Semver(int major, int minor, int patch, String prerelease) implements Comparable<Semver> {

        static Semver parse(String version) {
            String[] parts = version.split("-", 2);
            String[] nums = parts[0].split("\\.");
            int major = Integer.parseInt(nums[0]);
            int minor = nums.length > 1 ? Integer.parseInt(nums[1]) : 0;
            int patch = nums.length > 2 ? Integer.parseInt(nums[2]) : 0;
            String prerelease = parts.length > 1 ? parts[1] : null;
            return new Semver(major, minor, patch, prerelease);
        }

        @Override
        public int compareTo(Semver other) {
            int cmp = Integer.compare(major, other.major);
            if (cmp != 0) return cmp;
            cmp = Integer.compare(minor, other.minor);
            if (cmp != 0) return cmp;
            cmp = Integer.compare(patch, other.patch);
            if (cmp != 0) return cmp;
            // prerelease versions sort lower than release versions
            if (prerelease == null && other.prerelease != null) return 1;
            if (prerelease != null && other.prerelease == null) return -1;
            if (prerelease != null && other.prerelease != null) {
                return prerelease.compareTo(other.prerelease);
            }
            return 0;
        }
    }
}
