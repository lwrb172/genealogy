import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        List<Person> people = Person.fromCsv("family.csv");
//        for (Person person : people)
//            person.generateTree();
        people.stream().map(person -> person.getName());
        PlantUMLRunner.setPlantUMLPath("./plantuml-1.2024.3.jar");

//        PlantUMLRunner.generateDiagram(Person.generateUML(people), "./", "outUML");
//        Person.sortByLifespan(people).forEach(System.out::println);
//        System.out.println(Person.findOldestLiving(people));
        List<Person> deadPeople = Person.sortByLifespan(people);
        deadPeople.stream()
                .map(person -> person.getBirthDate().toEpochDay() - person.getDeathDate().toEpochDay())
                .collect(Collectors.toList()).forEach(System.out::println);

        Function<String, String> colorYellow = s -> s.contains("object")?s.trim()+" #Yellow \n":s;
        Function<String, String> noChange = Function.identity();
        Predicate<Person> hasNameStartingWithA = person -> person.getName().contains("Kowalsk");
        PlantUMLRunner.generateDiagram
                (Person.generateTree(people, colorYellow, hasNameStartingWithA),
                        "./","outUML");
    }
}