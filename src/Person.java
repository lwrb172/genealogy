import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Person implements Serializable {
    private final String name;
    private final LocalDate birthDate;
    private final LocalDate deathDate;
    private final List<Person> parents = new ArrayList<>();

    public Person(String name, LocalDate birthDate, LocalDate deathDate) {
        this.name = name;
        this.birthDate = birthDate;
        this.deathDate = deathDate;
    }

    public static Person fromCsvLine(String line) {
//        Marek Kowalski,15.05.1899,25.06.1957,,
//        Ewa Kowalska,03.11.1901,05.03.1990,,
        String[] parts = line.split(",", -1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate birthDate = LocalDate.parse(parts[1], formatter);
        LocalDate deathDate = parts[2].isEmpty() ? null : LocalDate.parse(parts[2], formatter);

        return new Person(parts[0], birthDate, deathDate);
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", birthDate=" + birthDate +
                ", deathDate=" + deathDate +
                ", parents=" + parents +
                '}';
    }

    public static List<Person> fromCsv(String path) {
        List<Person> people = new ArrayList<>();
        Map<String, PersonWithParentsNames> mapPersonWithParentNames = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                PersonWithParentsNames personWithNames = PersonWithParentsNames.fromCsvLine(line);
                personWithNames.getPerson().validateLifespan();
                personWithNames.getPerson().validateAmbiguity(people);

                Person person = personWithNames.getPerson();
                people.add(person);
                mapPersonWithParentNames.put(person.name, personWithNames);
            }
            PersonWithParentsNames.linkRelatives(mapPersonWithParentNames);
            try {
                for(Person person : people) {
                    person.validateParentingAge();
                }
            }
            catch (ParentingAgeException e) {
                Scanner scanner = new Scanner(System.in);
                System.out.println(e.getMessage());
                System.out.println("Please confirm [Y/N]:");
                String response = scanner.nextLine();
                if (!response.equals("Y") && !response.equals("y"))
                    people.remove(e.person);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NegativeLifespanException | AmbiguousPersonException e) {
            System.err.println(e.getMessage());
        }

        return people;
    }

    public String getName() {
        return name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public LocalDate getDeathDate() {
        return deathDate;
    }

    private void validateLifespan() throws NegativeLifespanException {
        if (deathDate != null && deathDate.isBefore(birthDate))
            throw new NegativeLifespanException(this);
    }

    private void validateAmbiguity(List<Person> people) throws AmbiguousPersonException {
        for (Person person : people)
            if (person.getName().equals(getName()))
                throw new AmbiguousPersonException(person);
    }

    private void validateParentingAge() throws ParentingAgeException {
        for (Person parent : parents)
            if (birthDate.isBefore(parent.birthDate.plusYears(15)) || (parent.deathDate != null && birthDate.isAfter(parent.deathDate)))
                throw new ParentingAgeException(this, parent);
    }

    public void addParent(Person parent) {
        parents.add(parent);
    }

    public static void toBinaryFile(List<Person> people, String filename) throws IOException {
        try (
                FileOutputStream fos = new FileOutputStream(filename);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(people);
        }
    }

    public static List<Person> fromBinaryFile(String filename) throws IOException, ClassNotFoundException {
        try (
                FileInputStream fis = new FileInputStream(filename);
                ObjectInputStream ois = new ObjectInputStream(fis);
        ) {
            return (List<Person>) ois.readObject();
        }
    }
    public String generateTree(){
        Function<Person,String> cleanPersonName = p -> p.getName().replaceAll(" ","");
        Function<Person, String> addObject = person -> String.format("object %s", cleanPersonName.apply(person));

        StringBuilder sb = new StringBuilder("@startuml\n");
        String pname = cleanPersonName.apply(this);
        sb.append(addObject.apply(this));

        if (!parents.isEmpty()) {
            String parentSections = parents.stream()
                    .map(parent ->"\n"+addObject.apply(parent)+"\n"+
                            cleanPersonName.apply(parent)+"<--"+pname )
                    .collect(Collectors.joining());
            sb.append(parentSections);
        }
        return sb.append("\n@enduml").toString();
    }

    public static String generateTree(List<Person> people, Function<String, String> postProcess, Predicate<Person> condition) {
        Function<Person,String> clean = p -> p.getName().replaceAll(" ","");
        Function<Person, String> addObject = person -> String.format("object %s\n", clean.apply(person));
        Function<Person, String> post = addObject.andThen(postProcess);
        String objects = people.stream()
                .map(person -> condition.test(person) ? post.apply(person) : addObject.apply(person))
                .collect(Collectors.joining());

        String relationships = people.stream()
                .flatMap(person ->
                        person.getParents().isEmpty() ?  Stream.empty():
                                person.getParents().stream()
                                        .map(parent -> String.format("%s <-- %s\n",clean.apply(parent),
                                                clean.apply(person)
                                        ))
                )
                .collect(Collectors.joining());

        return String.format("@startuml\n%s%s@enduml", objects, relationships);
    }

    public List<Person> getParents() {
        return parents;
    }

    public static List<Person> filterByName(List<Person> people, String substring) {
        return people.stream()
                .filter(person -> person.getName().contains(substring))
                .collect(Collectors.toList());
    }

    public static List<Person> sortedByBirth(List<Person> people){
        return people.stream()
                .sorted(Comparator.comparing(Person::getBirthDate))
                .collect(Collectors.toList());
    }

    public static List<Person> sortByLifespan(List<Person> people){

        Function<Person, Long> getLifespan = person
                -> person.deathDate.toEpochDay() - person.birthDate.toEpochDay();

        return people.stream()
                .filter(person -> person.deathDate != null)
                .sorted((o2, o1) -> Long.compare(getLifespan.apply(o1), getLifespan.apply(o2)))
                .toList();
    }

    public static Person findOldestLiving(List<Person> people) {
        return people.stream()
                .filter(person -> person.deathDate == null)
                .min(Comparator.comparing(Person::getBirthDate))
                .orElse(null);
    }
}
