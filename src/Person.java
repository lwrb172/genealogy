import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Person {
    private final String name;
    private final LocalDate birthDate;
    private final LocalDate deathDate;
    private List<Person> parents = new ArrayList<>();

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

    public static List<Person> fromCsv(String path) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(path));
        List<Person> people = new ArrayList<>();
        Map<String, PersonWithParentsNames> peopleWithParentNames = new HashMap<>();
        String line;
        reader.readLine();
        while ((line = reader.readLine()) != null) {
//            Person person = Person.fromCsvLine(line);
            var personWithParentNames = PersonWithParentsNames.fromCsvLine(line);
            var person = personWithParentNames.getPerson();
            try {
                person.validateLifespan();
                person.validateAmbiguity(people);
                people.add(Person.fromCsvLine(line));
                people.add(person);
                peopleWithParentNames.put(person.name, personWithParentNames);
            } catch (NegativeLifespanException | AmbiguousPersonException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
        PersonWithParentsNames.linkRelatives(peopleWithParentNames);
        reader.close();
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
        for (Person person : people) {
            if (person.getName().equals(getName()))
                throw new AmbiguousPersonException(person);
        }
    }

    public void addParent(Person parent) {
        parents.add(parent);
    }
}
