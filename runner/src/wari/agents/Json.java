package wari.agents;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Minimal JSON codec for local state; no external runtime dependency. */
final class Json {
    private Json() {}

    static Object parse(String source) {
        Parser parser = new Parser(source);
        Object value = parser.value();
        parser.space();
        if (parser.at != source.length()) throw new IllegalArgumentException("JSON trailing data");
        return value;
    }

    static String write(Object value) {
        if (value == null) return "null";
        if (value instanceof Boolean || value instanceof Number) return value.toString();
        if (value instanceof String s) {
            StringBuilder out = new StringBuilder("\"");
            for (char c : s.toCharArray()) {
                switch (c) {
                    case '"' -> out.append("\\\"");
                    case '\\' -> out.append("\\\\");
                    case '\n' -> out.append("\\n");
                    case '\r' -> out.append("\\r");
                    case '\t' -> out.append("\\t");
                    default -> {
                        if (c < 32) out.append(String.format("\\u%04x", (int)c));
                        else out.append(c);
                    }
                }
            }
            return out.append('"').toString();
        }
        if (value instanceof Map<?, ?> map) {
            StringBuilder out = new StringBuilder("{");
            for (var entry : map.entrySet()) {
                if (out.length() > 1) out.append(',');
                out.append(write(entry.getKey().toString())).append(':').append(write(entry.getValue()));
            }
            return out.append('}').toString();
        }
        if (value instanceof Iterable<?> values) {
            StringBuilder out = new StringBuilder("[");
            for (Object item : values) {
                if (out.length() > 1) out.append(',');
                out.append(write(item));
            }
            return out.append(']').toString();
        }
        throw new IllegalArgumentException("Unsupported JSON type: " + value.getClass());
    }

    private static final class Parser {
        private final String source;
        private int at;
        Parser(String source) { this.source = source; }
        void space() {
            while (at < source.length() && Character.isWhitespace(source.charAt(at))) at++;
        }
        char peek() {
            space();
            if (at >= source.length()) throw new IllegalArgumentException("Unexpected JSON end");
            return source.charAt(at);
        }
        Object value() {
            return switch (peek()) {
                case '{' -> object();
                case '[' -> array();
                case '"' -> string();
                case 't' -> literal("true", true);
                case 'f' -> literal("false", false);
                case 'n' -> literal("null", null);
                default -> number();
            };
        }
        Object literal(String word, Object value) {
            if (!source.startsWith(word, at)) throw new IllegalArgumentException("Invalid JSON literal");
            at += word.length();
            return value;
        }
        Map<String, Object> object() {
            at++;
            Map<String, Object> out = new LinkedHashMap<>();
            if (peek() == '}') { at++; return out; }
            while (true) {
                if (peek() != '"') throw new IllegalArgumentException("Expected JSON key");
                String key = string();
                if (peek() != ':') throw new IllegalArgumentException("Expected colon");
                at++;
                if (out.containsKey(key)) throw new IllegalArgumentException("Duplicate JSON key");
                out.put(key, value());
                char end = peek(); at++;
                if (end == '}') return out;
                if (end != ',') throw new IllegalArgumentException("Expected comma");
            }
        }
        List<Object> array() {
            at++;
            List<Object> out = new ArrayList<>();
            if (peek() == ']') { at++; return out; }
            while (true) {
                out.add(value());
                char end = peek(); at++;
                if (end == ']') return out;
                if (end != ',') throw new IllegalArgumentException("Expected comma");
            }
        }
        String string() {
            at++;
            StringBuilder out = new StringBuilder();
            while (at < source.length()) {
                char c = source.charAt(at++);
                if (c == '"') return out.toString();
                if (c != '\\') { if (c < 32) throw new IllegalArgumentException("Control character"); out.append(c); continue; }
                if (at >= source.length()) throw new IllegalArgumentException("Incomplete escape");
                char e = source.charAt(at++);
                switch (e) {
                    case '"', '\\', '/' -> out.append(e);
                    case 'b' -> out.append('\b');
                    case 'f' -> out.append('\f');
                    case 'n' -> out.append('\n');
                    case 'r' -> out.append('\r');
                    case 't' -> out.append('\t');
                    case 'u' -> {
                        if (at + 4 > source.length()) throw new IllegalArgumentException("Bad unicode escape");
                        out.append((char)Integer.parseInt(source.substring(at, at + 4), 16)); at += 4;
                    }
                    default -> throw new IllegalArgumentException("Bad escape");
                }
            }
            throw new IllegalArgumentException("Unterminated string");
        }
        Number number() {
            int start = at;
            while (at < source.length() && "-+0123456789.eE".indexOf(source.charAt(at)) >= 0) at++;
            if (start == at) throw new IllegalArgumentException("Invalid JSON value");
            try {
                double value = Double.parseDouble(source.substring(start, at));
                if (!Double.isFinite(value)) throw new NumberFormatException("Non-finite number");
                return value;
            }
            catch (NumberFormatException e) { throw new IllegalArgumentException("Invalid number", e); }
        }
    }
}
