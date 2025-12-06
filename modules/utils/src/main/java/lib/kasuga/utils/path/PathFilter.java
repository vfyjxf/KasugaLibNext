package lib.kasuga.utils.path;

import java.util.Objects;
import java.util.regex.Pattern;

public interface PathFilter {

    /*
        @return int -1 = reject, 0 = weak accept, 1 = accept
     */
    public Result test(String pathPart);

    public String toString();

    public default boolean willMatchSelf() {
        return false;
    }

    public static enum Result {
        INITIAL,
        REJECT,
        RECURSIVE_ACCEPT,
        ACCEPT,
        RETURN
    }


    public class GlobPathFilter implements PathFilter {

        private final boolean wildcardRecursive;
        private final Pattern pattern;
        private final String globString;

        public GlobPathFilter(String glob) {
            if (glob == null) {
                throw new IllegalArgumentException("Glob pattern cannot be null");
            }

            this.globString = glob;

            if ("**".equals(glob)) {
                this.wildcardRecursive = true;
                this.pattern = null;
            } else {
                this.wildcardRecursive = false;
                this.pattern = Pattern.compile(globToRegex(glob));
            }
        }

        @Override
        public Result test(String pathPart) {
            if (wildcardRecursive) {
                return Result.RECURSIVE_ACCEPT;
            }

            if (pattern != null && pattern.matcher(pathPart).matches()) {
                return Result.ACCEPT;
            }

            return Result.REJECT;
        }

        private static String globToRegex(String glob) {
            StringBuilder sb = new StringBuilder("^");
            for (char c : glob.toCharArray()) {
                switch (c) {
                    case '*':
                        sb.append(".*");
                        break;
                    case '?':
                        sb.append(".");
                        break;
                    // 转义正则中的特殊字符
                    case '.':
                    case '(':
                    case ')':
                    case '+':
                    case '|':
                    case '^':
                    case '$':
                    case '@':
                    case '%':
                    case '{':
                    case '}':
                    case '[':
                    case ']':
                    case '\\':
                        sb.append('\\').append(c);
                        break;
                    default:
                        sb.append(c);
                        break;
                }
            }
            sb.append('$');
            return sb.toString();
        }

        @Override
        public String toString() {
            return "GlobPathFilter{"+ globString + '}';
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof GlobPathFilter that)) return false;
            return wildcardRecursive == that.wildcardRecursive && Objects.equals(pattern, that.pattern);
        }

        @Override
        public int hashCode() {
            return Objects.hash(wildcardRecursive, pattern);
        }

        @Override
        public boolean willMatchSelf() {
            return wildcardRecursive;
        }
    }

    public static class InitPathFilter implements PathFilter {

        @Override
        public Result test(String pathPart) {
            return Result.INITIAL;
        }

        @Override
        public String toString() {
            return "InitPathFilter{}";
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof InitPathFilter)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return Objects.hash(InitPathFilter.class);
        }
    }

    public static InitPathFilter INIT = new InitPathFilter();
}
