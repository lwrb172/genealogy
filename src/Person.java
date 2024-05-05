import org.w3c.dom.ls.LSInput;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Person {
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
                System.out.println("Please confitm [Y/N]:");
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
}
