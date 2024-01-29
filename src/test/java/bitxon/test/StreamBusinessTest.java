package bitxon.test;

import bitxon.model.business.Department;
import bitxon.model.business.Employee;
import bitxon.model.Pair;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class StreamBusinessTest {

    static final String SENIOR = "Senior";
    static final String MIDDLE = "Middle";
    static final String JUNIOR = "Junior";

    static final List<Employee> EMPLOYEES_1 = List.of(
        Employee.builder().title(SENIOR).salary(10_000).build(),
        Employee.builder().title(MIDDLE).salary(7_400).build(),
        Employee.builder().title(MIDDLE).salary(7_400).build(),
        Employee.builder().title(JUNIOR).salary(4_000).build(),
        Employee.builder().title(JUNIOR).salary(3_900).build(),
        Employee.builder().title(SENIOR).salary(9_500).build(),
        Employee.builder().title(SENIOR).salary(9_500).build(),
        Employee.builder().title(SENIOR).salary(8_300).build()
    );

    static final List<Employee> EMPLOYEES_2_NO_JUNIORS = List.of(
        Employee.builder().title(MIDDLE).salary(7_400).build(),
        Employee.builder().title(SENIOR).salary(11_100).build(),
        Employee.builder().title(SENIOR).salary(8_900).build(),
        Employee.builder().title(SENIOR).salary(8_900).build()
    );

    static final List<Employee> EMPLOYEES_2_NO_MIDDLES = List.of(
        Employee.builder().title(SENIOR).salary(10_000).build(),
        Employee.builder().title(JUNIOR).salary(4_000).build(),
        Employee.builder().title(JUNIOR).salary(3_900).build(),
        Employee.builder().title(SENIOR).salary(9_500).build(),
        Employee.builder().title(SENIOR).salary(8_300).build()
    );


    static final String FINANCE = "Finance";
    static final String IT = "IT";
    static final String HR = "H&R";
    static final List<Department> DEPARTMENTS = List.of(
        Department.builder().name(FINANCE).employees(EMPLOYEES_1).build(),
        Department.builder().name(IT).employees(EMPLOYEES_2_NO_MIDDLES).build(),
        Department.builder().name(HR).employees(EMPLOYEES_2_NO_JUNIORS).build()
    );

    public static final Comparator<IntSummaryStatistics> INT_SUMMARY_STATISTICS_COMPARATOR = Comparator
        .comparing(IntSummaryStatistics::getCount)
        .thenComparingInt(IntSummaryStatistics::getMin)
        .thenComparingInt(IntSummaryStatistics::getMax)
        .thenComparingLong(IntSummaryStatistics::getSum)
        .thenComparingDouble(IntSummaryStatistics::getAverage);

    @Test
    void averageSalaryByTitle() {
        var statistic = DEPARTMENTS.stream()
            .map(Department::getEmployees)
            .flatMap(Collection::stream)
            .collect(Collectors.groupingBy(
                Employee::getTitle,
                Collectors.averagingInt(Employee::getSalary)));

        // Validate
        assertThat(statistic).isNotNull().containsExactlyInAnyOrderEntriesOf(Map.of(
            SENIOR, 9400d,
            MIDDLE, 7400d,
            JUNIOR, 3950d
        ));
    }

    @Test
    void minSalaryByTitle() {
        var statistic = DEPARTMENTS.stream()
            .map(Department::getEmployees)
            .flatMap(Collection::stream)
            .collect(Collectors.groupingBy(
                Employee::getTitle,
                Collectors.reducing(Integer.MAX_VALUE, Employee::getSalary, Integer::min)));

        // Validate
        assertThat(statistic).isNotNull().containsExactlyInAnyOrderEntriesOf(Map.of(
            SENIOR, 8300,
            MIDDLE, 7400,
            JUNIOR, 3900
        ));
    }

    @Test
    void averageSalaryByDepartment() {
        var statistic = DEPARTMENTS.stream()
            .collect(Collectors.groupingBy(
                Department::getName,
                Collectors.averagingDouble(e -> e.getEmployees().stream()
                    .collect(Collectors.averagingDouble(Employee::getSalary))
                )
            ));

        // Validate
        assertThat(statistic).isNotNull().containsExactlyInAnyOrderEntriesOf(Map.of(
            FINANCE, 7500d,
            IT, 7140d,
            HR, 9075d
        ));
    }

    @Test
    void statisticSalaryByTitle() {
        var statistic = DEPARTMENTS.stream()
            .map(Department::getEmployees)
            .flatMap(Collection::stream)
            .collect(Collectors.groupingBy(
                Employee::getTitle,
                Collectors.summarizingInt(Employee::getSalary)));

        // Validate
        assertThat(statistic).isNotNull().containsOnlyKeys(JUNIOR, MIDDLE, SENIOR);
        assertThat(statistic.get(JUNIOR)).as("Junior check")
            .usingComparator(INT_SUMMARY_STATISTICS_COMPARATOR)
            .isEqualTo(new IntSummaryStatistics(4, 3900, 4000, 15800));
        assertThat(statistic.get(MIDDLE)).as("Middle check")
            .usingComparator(INT_SUMMARY_STATISTICS_COMPARATOR)
            .isEqualTo(new IntSummaryStatistics(3, 7400, 7400, 22200));
        assertThat(statistic.get(SENIOR)).as("Senior check").isNotNull()
            .usingComparator(INT_SUMMARY_STATISTICS_COMPARATOR)
            .isEqualTo(new IntSummaryStatistics(10, 8300, 11100, 94000));
    }

    @Test
    void minSalaryByTitle_CustomCollector() {
        var statistic = DEPARTMENTS.parallelStream()
            .map(Department::getEmployees)
            .flatMap(Collection::stream)
            .collect(Collectors.groupingBy(
                Employee::getTitle,
                Collector.of(
                    () -> new int[]{Integer.MAX_VALUE},
                    (result, article) -> result[0] = Math.min(result[0], article.getSalary()), // regular stream
                    (result1, result2) -> new int[]{Math.min(result1[0], result2[0])}, // parallel stream
                    total -> total[0]
                )
            ));

        // Validate
        assertThat(statistic).isNotNull().containsExactlyInAnyOrderEntriesOf(Map.of(
            SENIOR, 8300,
            MIDDLE, 7400,
            JUNIOR, 3900
        ));
    }

    @Test
    void minAndMaxSalaryByTitle_CustomCollector() {
        var statistic = DEPARTMENTS.stream()
            .map(Department::getEmployees)
            .flatMap(Collection::stream)
            .collect(Collectors.groupingBy(
                Employee::getTitle,
                Collector.of(
                    () -> new int[]{Integer.MAX_VALUE, Integer.MIN_VALUE},
                    (result, employee) -> {
                        result[0] = Math.min(result[0], employee.getSalary());
                        result[1] = Math.max(result[1], employee.getSalary());
                    }, // regular stream
                    (result1, result2) -> new int[]{
                        Math.min(result1[0], result2[0]),
                        Math.max(result1[1], result2[1])
                    }, // parallel stream
                    total -> new Pair<>(total[0], total[1])
                )
            ));

        // Validate
        assertThat(statistic).isNotNull().containsOnlyKeys(JUNIOR, MIDDLE, SENIOR);
        assertThat(statistic.get(JUNIOR)).as("Junior check")
            .isEqualTo(new Pair(3900, 4000));
        assertThat(statistic.get(MIDDLE)).as("Middle check")
            .isEqualTo(new Pair(7400, 7400));
        assertThat(statistic.get(SENIOR)).as("Senior check")
            .isEqualTo(new Pair(8300, 11100));
    }

    @Test
    void sumSalaries() {
        var reduceResult = DEPARTMENTS.stream()
            .map(Department::getEmployees)
            .flatMap(Collection::stream)
            .map(Employee::getSalary)
            .reduce(Integer::sum)
            .orElseThrow();
        assertThat(reduceResult).as("Reduce check").isEqualTo(132000);

        var intStreamSumResult = DEPARTMENTS.stream()
            .map(Department::getEmployees)
            .flatMap(Collection::stream)
            .mapToInt(Employee::getSalary)
            .sum();
        assertThat(intStreamSumResult).as("IntStream.sum() check").isEqualTo(132000);

        var collectorResult = DEPARTMENTS.stream()
            .map(Department::getEmployees)
            .flatMap(Collection::stream)
            .map(Employee::getSalary)
            .collect(Collectors.summingInt(Integer::intValue));
        assertThat(collectorResult).as("collector check").isEqualTo(132000);

    }

    @Test
    void allDepartmentsHasAtLeastOneJunior() {
        var departmentsWithJuniors = List.of(
            Department.builder().employees(EMPLOYEES_1).build(),
            Department.builder().employees(EMPLOYEES_2_NO_MIDDLES).build()
        );
        var departmentsWithoutJuniors = List.of(
            Department.builder().employees(EMPLOYEES_1).build(),
            Department.builder().employees(EMPLOYEES_2_NO_JUNIORS).build()
        );

        var result1 = departmentsWithJuniors.stream()
            .allMatch(department -> department.getEmployees().stream()
                .anyMatch(employee -> JUNIOR.equals(employee.getTitle())));
        assertThat(result1).isTrue();

        var result2 = departmentsWithoutJuniors.stream()
            .allMatch(department -> department.getEmployees().stream()
                .anyMatch(employee -> JUNIOR.equals(employee.getTitle())));
        assertThat(result2).isFalse();
    }


    @Test
    void mutateToMonsterCompanyHahahahaha() {
        var result = DEPARTMENTS.stream()
            .map(Department::getEmployees)
            .flatMap(Collection::stream)
            .reduce(
                Employee.builder().title("").salary(0).build(),
                (accumulator, employee) -> Employee.builder()
                    .title(accumulator.getTitle() + employee.getTitle())
                    .salary(accumulator.getSalary() + employee.getSalary())
                    .build());

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("SeniorMiddleMiddleJuniorJuniorSeniorSeniorSeniorSeniorJuniorJuniorSeniorSeniorMiddleSeniorSeniorSenior");
        assertThat(result.getSalary()).isEqualTo(132000);
    }

    @Test
    void findTwoWithHighestSalary() {
        var highlyPaidEmployees = DEPARTMENTS.stream()
            .map(Department::getEmployees)
            .flatMap(Collection::stream)
            .sorted(Comparator.comparing(Employee::getSalary).reversed())
            .limit(2)
            .collect(Collectors.toList());

        assertThat(highlyPaidEmployees).containsExactly(
            Employee.builder().title(SENIOR).salary(11_100).build(),
            Employee.builder().title(SENIOR).salary(10_000).build()
        );

    }


}
