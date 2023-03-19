import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class OnlineCoursesAnalyzer {

    List<Course> courses = new ArrayList<>();

    public OnlineCoursesAnalyzer(String datasetPath) {
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(datasetPath, StandardCharsets.UTF_8));
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
                Course course = new Course(info[0], info[1],
                        new Date(info[2]), info[3], info[4], info[5],
                        Integer.parseInt(info[6]),
                        Integer.parseInt(info[7]),
                        Integer.parseInt(info[8]),
                        Integer.parseInt(info[9]),
                        Integer.parseInt(info[10]),
                        Double.parseDouble(info[11]),
                        Double.parseDouble(info[12]),
                        Double.parseDouble(info[13]),
                        Double.parseDouble(info[14]),
                        Double.parseDouble(info[15]),
                        Double.parseDouble(info[16]),
                        Double.parseDouble(info[17]),
                        Double.parseDouble(info[18]),
                        Double.parseDouble(info[19]),
                        Double.parseDouble(info[20]),
                        Double.parseDouble(info[21]),
                        Double.parseDouble(info[22]));
                courses.add(course);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //1
    public Map<String, Integer> getPtcpCountByInst() {
        Map<String, Integer> ptcpCountByInst = new TreeMap<>();

        for (Course course : courses) {
            ptcpCountByInst
                    .put(course.institution, ptcpCountByInst
                            .getOrDefault(course.institution, 0) + course.participants);
        }
        return ptcpCountByInst;
    }

    //2
    public Map<String, Integer> getPtcpCountByInstAndSubject() {
        Map<String, Integer> ptcpCountByInstAndSubject = new TreeMap<>(Collections.reverseOrder());

        for (Course course : courses) {
            String institution = course.institution;
            String subject = course.subject;
            int ptcpCount = course.participants;

            String instAndSubject = institution + "-" + subject;
            ptcpCountByInstAndSubject
                    .put(instAndSubject, ptcpCountByInstAndSubject
                            .getOrDefault(instAndSubject, 0) + ptcpCount);
        }

        ptcpCountByInstAndSubject = ptcpCountByInstAndSubject.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        return ptcpCountByInstAndSubject;
    }

    //3
    public Map<String, List<List<String>>> getCourseListOfInstructor() {
        Map<String, List<List<String>>> courseListByInstructor = new HashMap<>();

        for (Course course : courses) {

            String courseTitle = course.title;
            boolean isIndependent = !course.instructors.contains(",");

            String[] instructorList = course.instructors.split(", ");

            for (String instructor : instructorList) {
                courseListByInstructor.putIfAbsent(instructor, new ArrayList<>());
                List<List<String>> courseLists = courseListByInstructor.get(instructor);
                while (courseLists.size() < 2) {
                    courseLists.add(new ArrayList<>());
                }
                if (isIndependent) {
                    if (!courseLists.get(0).contains(courseTitle)) {
                        courseLists.get(0).add(courseTitle);
                    }
                } else {
                    if (!courseLists.get(1).contains(courseTitle)) {
                        courseLists.get(1).add(courseTitle);
                    }
                }
            }

        }

        for (List<List<String>> courseLists : courseListByInstructor.values()) {
            for (List<String> courseList : courseLists) {
                Collections.sort(courseList);
            }
        }

        return courseListByInstructor;
    }

    //4
    public List<String> getCourses(int topK, String by) {
        Comparator<Course> comparator;
        if (by.equals("hours")) {
            comparator = Comparator.comparing(Course::getTotalHours).reversed()
                    .thenComparing(Course::getTitle);
        } else if (by.equals("participants")) {
            comparator = Comparator.comparing(Course::getParticipants).reversed()
                    .thenComparing(Course::getTitle);
        } else {
            throw new IllegalArgumentException("Invalid sorting criteria: " + by);
        }
        List<Course> sortedCourses = courses.stream().sorted(comparator).collect(Collectors.toList());
        Set<String> courseTitles = new LinkedHashSet<>();
        for (Course course : sortedCourses) {
            courseTitles.add(course.getTitle());
        }
        return new ArrayList<>(courseTitles).subList(0,topK);
    }



    //5
    public List<String> searchCourses(String courseSubject,
                                      double percentAudited,
                                      double totalCourseHours) {
        List<String> result = new ArrayList<>();
        for (Course course : courses) {
            if (course.subject.toLowerCase().contains(courseSubject.toLowerCase())
                    && course.percentAudited >= percentAudited
                    && course.totalHours <= totalCourseHours) {
                result.add(course.getTitle());
            }
        }
        Set<String> resultSet = new TreeSet<>(result);
        return new ArrayList<>(resultSet);
    }


    //6
    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
        for (Course course : courses) {
            double avgMedianAge = course.medianAge;
            double avgGender100 = course.percentMale;
            double avgIsBachelorOrHigher100 = course.percentDegree;
            int numParticipants = 1;
            for (Course otherCourse : courses) {
                if (course.getCourseNumber().equals(otherCourse.getCourseNumber())
                        && course.getLaunchDate().before(otherCourse.getLaunchDate())) {
                    course = otherCourse;
                }
                if (course.getCourseNumber().equals(otherCourse.getCourseNumber())
                        && course.getLaunchDate().equals(otherCourse.getLaunchDate())) {
                    continue;
                }
                if (course.getCourseNumber().equals(otherCourse.getCourseNumber())) {
                    avgMedianAge += otherCourse.getMedianAge();
                    avgGender100 += otherCourse.getPercentMale();
                    avgIsBachelorOrHigher100 += otherCourse.getPercentDegree();
                    numParticipants++;
                }
            }
            course.setAvgMedianAge(avgMedianAge / numParticipants);
            course.setAvgGender100(avgGender100 / numParticipants);
            course.setAvgIsBachelorOrHigher100(avgIsBachelorOrHigher100 / numParticipants);
        }

        Map<Course, Double> similarityValues = new HashMap<>();
        for (Course course : courses) {
            double similarityValue = Math.pow((age - course.getAvgMedianAge()), 2) +
                    Math.pow((gender * 100 - course.getAvgGender100()), 2) +
                    Math.pow((isBachelorOrHigher * 100 - course.getAvgIsBachelorOrHigher100()), 2);
            similarityValues.put(course, similarityValue);
        }

        List<Course> sortedCourses = similarityValues.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<String> recommendedCourses = new ArrayList<>();
        Set<String> courseTitles = new HashSet<>();
        for (int i = 0; recommendedCourses.size() < 10; i++) {
            Course course = sortedCourses.get(i);
            String courseTitle = course.getTitle();
            if (courseTitles.contains(courseTitle)) {
                continue;
            }
            recommendedCourses.add(courseTitle);
            courseTitles.add(courseTitle);
        }
        return recommendedCourses;
    }

}

class Course {
    String institution;
    String number;
    Date launchDate;
    String title;
    String instructors;
    String subject;
    int year;
    int honorCode;
    int participants;
    int audited;
    int certified;
    double percentAudited;
    double percentCertified;
    double percentCertified50;
    double percentVideo;
    double percentForum;
    double gradeHigherZero;
    double totalHours;
    double medianHoursCertification;
    double medianAge;
    double percentMale;
    double percentFemale;
    double percentDegree;
    double avgMedianAge;
    double avgGender100;
    double avgIsBachelorOrHigher100;

    public Course(String institution, String number, Date launchDate,
                  String title, String instructors, String subject,
                  int year, int honorCode, int participants,
                  int audited, int certified, double percentAudited,
                  double percentCertified, double percentCertified50,
                  double percentVideo, double percentForum, double gradeHigherZero,
                  double totalHours, double medianHoursCertification,
                  double medianAge, double percentMale, double percentFemale,
                  double percentDegree) {
        this.institution = institution;
        this.number = number;
        this.launchDate = launchDate;
        if (title.startsWith("\"")) title = title.substring(1);
        if (title.endsWith("\"")) title = title.substring(0, title.length() - 1);
        this.title = title;
        if (instructors.startsWith("\"")) instructors = instructors.substring(1);
        if (instructors.endsWith("\"")) instructors = instructors.substring(0, instructors.length() - 1);
        this.instructors = instructors;
        if (subject.startsWith("\"")) subject = subject.substring(1);
        if (subject.endsWith("\"")) subject = subject.substring(0, subject.length() - 1);
        this.subject = subject;
        this.year = year;
        this.honorCode = honorCode;
        this.participants = participants;
        this.audited = audited;
        this.certified = certified;
        this.percentAudited = percentAudited;
        this.percentCertified = percentCertified;
        this.percentCertified50 = percentCertified50;
        this.percentVideo = percentVideo;
        this.percentForum = percentForum;
        this.gradeHigherZero = gradeHigherZero;
        this.totalHours = totalHours;
        this.medianHoursCertification = medianHoursCertification;
        this.medianAge = medianAge;
        this.percentMale = percentMale;
        this.percentFemale = percentFemale;
        this.percentDegree = percentDegree;

    }

    public String getTitle() {
        return this.title;
    }

    public int getParticipants() {
        return this.participants;
    }

    public double getTotalHours() {
        return this.totalHours;
    }

    public String getCourseNumber() {
        return this.number;
    }

    public double getMedianAge() {
        return medianAge;
    }

    public double getPercentDegree() {
        return percentDegree;
    }

    public Date getLaunchDate() {
        return launchDate;
    }

    public double getAvgGender100() {
        return avgGender100;
    }

    public double getAvgIsBachelorOrHigher100() {
        return avgIsBachelorOrHigher100;
    }

    public double getAvgMedianAge() {
        return avgMedianAge;
    }

    public double getPercentMale() {
        return percentMale;
    }

    public void setAvgGender100(double avgGender100) {
        this.avgGender100 = avgGender100;
    }

    public void setAvgIsBachelorOrHigher100(double avgIsBachelorOrHigher100) {
        this.avgIsBachelorOrHigher100 = avgIsBachelorOrHigher100;
    }

    public void setAvgMedianAge(double avgMedianAge) {
        this.avgMedianAge = avgMedianAge;
    }
}