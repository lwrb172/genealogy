import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Person> people = Person.fromCsv("family.csv");
//        for (Person person : people)
//            person.generateTree();

        PlantUMLRunner.setPlantUMLPath("./plantuml-1.2024.3.jar");

//        PlantUMLRunner.generateDiagram(Person.generateUML(people), "./", "outUML");
//        Person.sortByLifespan(people).forEach(System.out::println);
        System.out.println(Person.findOldestLiving(people));
    }
}