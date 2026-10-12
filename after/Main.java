import java.util.ArrayList;

/*
 * Клуб знакомств молодых книголюбов — ПОСЛЕ рефакторинга (паттерн Observer)
 *
 * Было: NotifierFactory с цепочкой if / else if, которая росла при добавлении
 * нового канала связи (telegram, sms, email, ...).
 *
 * Стало: каждый канал связи — это наблюдатель (ClubObserver).
 * NewsBoard (издатель) ничего не знает о конкретных каналах и просто
 * вызывает update() у всех подписчиков. Новый канал = новый класс,
 * существующий код не меняется.
 *
 * Паттерны в проекте:
 *   1. Singleton — ClubDatabase: одна общая база пользователей
 *   2. Strategy  — MatchStrategy: разные способы подбора пары
 *   3. Observer  — NewsBoard + ClubObserver: рассылка новостей клуба
 */

// ======================= ГЛАВНЫЙ КЛАСС ========================

public class Main {
    public static void main(String[] args) {
        ClubDatabase db = ClubDatabase.getInstance();

        // --- Создаём пользователей (у каждого свой канал-наблюдатель) ---
        User aigerim = new User("Айгерим", 20, "Алматы", new TelegramObserver("@aigerim_reads"));
        aigerim.addGenre("фэнтези");
        aigerim.addGenre("классика");
        aigerim.addGenre("поэзия");

        User arman = new User("Арман", 22, "Алматы", new EmailObserver("arman@mail.kz"));
        arman.addGenre("фэнтези");
        arman.addGenre("фантастика");
        arman.addGenre("классика");

        User dana = new User("Дана", 19, "Астана", new SmsObserver("+77010000000"));
        dana.addGenre("детектив");
        dana.addGenre("романтика");

        User timur = new User("Тимур", 24, "Алматы", new EmailObserver("timur@mail.kz"));
        timur.addGenre("фантастика");
        timur.addGenre("классика");

        User viktor = new User("Виктор", 45, "Алматы", new EmailObserver("viktor@mail.kz"));
        viktor.addGenre("классика");

        // --- 1. Singleton: регистрация в общей базе ---
        System.out.println("=== 1. Регистрация (Singleton) ===");
        db.register(aigerim);
        db.register(arman);
        db.register(dana);
        db.register(timur);
        db.register(viktor); // не пройдёт: возраст больше 30
        System.out.println("Всего участников: " + db.getUsers().size());

        ClubDatabase sameDb = ClubDatabase.getInstance();
        System.out.println("Это та же самая база? " + (db == sameDb));

        // --- 2. Strategy: подбор пары разными способами ---
        System.out.println();
        System.out.println("=== 2. Подбор пары для Айгерим (Strategy) ===");
        Matchmaker matchmaker = new Matchmaker(new GenreMatchStrategy());
        matchmaker.printMatches(aigerim, db.getUsers());

        matchmaker.setStrategy(new CityAgeMatchStrategy());
        matchmaker.printMatches(aigerim, db.getUsers());

        // --- 3. Observer: уведомления о знакомстве через каналы-наблюдатели ---
        System.out.println();
        System.out.println("=== 3. Уведомления о знакомстве (Observer) ===");
        User best = matchmaker.findBest(aigerim, db.getUsers());
        if (best != null) {
            aigerim.getObserver().update("Познакомьтесь с " + best.getName() + "!");
            best.getObserver().update("Познакомьтесь с " + aigerim.getName() + "!");
        }

        // --- 4. Observer: новости клуба ---
        System.out.println();
        System.out.println("=== 4. Новости клуба (Observer) ===");
        NewsBoard board = new NewsBoard();
        for (User u : db.getUsers()) {
            board.subscribe(u.getObserver());
        }
        board.publish("Встреча клуба: обсуждаем «Мастер и Маргарита», суббота 18:00");

        board.unsubscribe(dana.getObserver());
        board.publish("Новая книга месяца: «Маленький принц»");
    }
}

// ======================= МОДЕЛЬ ================================

class User {
    private String name;
    private int age;
    private String city;
    private ClubObserver observer;   // канал связи пользователя (вместо строки "email"/"sms")
    private ArrayList<String> genres = new ArrayList<>();

    public User(String name, int age, String city, ClubObserver observer) {
        this.name = name;
        this.age = age;
        this.city = city;
        this.observer = observer;
    }

    public void addGenre(String genre) {
        genres.add(genre);
    }

    public String getName()              { return name; }
    public int getAge()                  { return age; }
    public String getCity()              { return city; }
    public ClubObserver getObserver()    { return observer; }
    public ArrayList<String> getGenres() { return genres; }

    @Override
    public String toString() {
        return name + ", " + age + ", " + city;
    }
}

// ======================= 1. SINGLETON ==========================

class ClubDatabase {
    private static ClubDatabase instance;           // единственный объект
    private ArrayList<User> users = new ArrayList<>();

    private ClubDatabase() { }                      // конструктор закрыт

    public static ClubDatabase getInstance() {
        if (instance == null) {
            instance = new ClubDatabase();
        }
        return instance;
    }

    public void register(User user) {
        if (user.getAge() < 14 || user.getAge() > 30) {
            System.out.println("Отказ: " + user.getName() + " — клуб для молодёжи от 14 до 30 лет");
            return;
        }
        users.add(user);
        System.out.println("Зарегистрирован: " + user);
    }

    public ArrayList<User> getUsers() {
        return users;
    }
}

// ======================= 2. STRATEGY ===========================

interface MatchStrategy {
    int score(User a, User b);   // сколько баллов совместимости
    String getName();
}

// Совместимость по общим жанрам: 10 баллов за каждый общий жанр
class GenreMatchStrategy implements MatchStrategy {
    public int score(User a, User b) {
        int points = 0;
        for (String genre : a.getGenres()) {
            if (b.getGenres().contains(genre)) {
                points += 10;
            }
        }
        return points;
    }

    public String getName() { return "по жанрам"; }
}

// Совместимость по городу и возрасту
class CityAgeMatchStrategy implements MatchStrategy {
    public int score(User a, User b) {
        int points = 0;
        if (a.getCity().equals(b.getCity())) {
            points += 20;
        }
        int ageDiff = Math.abs(a.getAge() - b.getAge());
        if (ageDiff <= 2) {
            points += 10;
        } else if (ageDiff <= 5) {
            points += 5;
        }
        return points;
    }

    public String getName() { return "по городу и возрасту"; }
}

// Класс, который использует стратегию
class Matchmaker {
    private MatchStrategy strategy;

    public Matchmaker(MatchStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(MatchStrategy strategy) {
        this.strategy = strategy;
    }

    public void printMatches(User user, ArrayList<User> all) {
        System.out.println("Подбор " + strategy.getName() + ":");
        for (User other : all) {
            if (other != user) {
                int points = strategy.score(user, other);
                System.out.println("  " + other.getName() + " — " + points + " баллов");
            }
        }
    }

    public User findBest(User user, ArrayList<User> all) {
        User best = null;
        int bestScore = 0;
        for (User other : all) {
            if (other != user) {
                int points = strategy.score(user, other);
                if (points > bestScore) {
                    bestScore = points;
                    best = other;
                }
            }
        }
        return best;
    }
}

// ======================= 3. OBSERVER ===========================

// Интерфейс наблюдателя: любой канал связи
interface ClubObserver {
    void update(String message);
    String getContact();
}

// Конкретные наблюдатели (каналы связи)
class EmailObserver implements ClubObserver {
    private String contact;

    public EmailObserver(String contact) { this.contact = contact; }

    public void update(String message) {
        System.out.println("  [Email " + contact + "] " + message);
    }

    public String getContact() { return contact; }
}

class TelegramObserver implements ClubObserver {
    private String contact;

    public TelegramObserver(String contact) { this.contact = contact; }

    public void update(String message) {
        System.out.println("  [Telegram " + contact + "] " + message);
    }

    public String getContact() { return contact; }
}

class SmsObserver implements ClubObserver {
    private String contact;

    public SmsObserver(String contact) { this.contact = contact; }

    public void update(String message) {
        System.out.println("  [SMS " + contact + "] " + message);
    }

    public String getContact() { return contact; }
}

// Издатель: не знает про конкретные каналы, только про интерфейс
class NewsBoard {
    private ArrayList<ClubObserver> subscribers = new ArrayList<>();

    public void subscribe(ClubObserver observer) {
        subscribers.add(observer);
    }

    public void unsubscribe(ClubObserver observer) {
        subscribers.remove(observer);
        System.out.println(observer.getContact() + " отписался(ась) от новостей");
    }

    public void publish(String news) {
        System.out.println("Новость: " + news);
        for (ClubObserver observer : subscribers) {
            observer.update(news);
        }
    }
}
