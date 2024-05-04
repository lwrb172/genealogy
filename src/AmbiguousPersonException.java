public class AmbiguousPersonException extends Exception{
    public AmbiguousPersonException(Person person) {
        super("More than one person with the same name: " + person.getName());
    }
}
