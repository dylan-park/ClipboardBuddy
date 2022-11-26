import java.util.Arrays;
import java.util.Objects;

public class Rule {
    String name;
    String regex;
    String[] replace;
    boolean disabled;

    public Rule(String name, String regex, String[] replace, boolean disabled) {
        this.name = name;
        this.regex = regex;
        this.replace = replace;
        this.disabled = disabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rule rule = (Rule) o;
        return disabled == rule.disabled && name.equals(rule.name) && regex.equals(rule.regex) && Arrays.equals(replace, rule.replace);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name, regex, disabled);
        result = 31 * result + Arrays.hashCode(replace);
        return result;
    }

    @Override
    public String toString() {
        return "Rule{" + "name='" + name + '\'' + ", regex='" + regex + '\'' + ", replace=" + Arrays.toString(replace) + ", disabled=" + disabled + '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String[] getReplace() {
        return replace;
    }

    public void setReplace(String[] replace) {
        this.replace = replace;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
