import java.util.ArrayList;

/*
 * Клуб знакомств молодых книголюбов (упрощённая версия)
 *
 * В проекте 4 простых паттерна:
 *   1. Singleton — ClubDatabase: одна общая база пользователей
 *   2. Factory   — NotifierFactory: создаёт нужный способ уведомления
 *   3. Strategy  — MatchStrategy: разные способы подбора пары
 *   4. Observer  — NewsBoard: рассылка новостей клуба подписчикам
 */

// ======================= ГЛАВНЫЙ КЛАСС ========================

public class Main {
    public static void main(String[] args) {
        ClubDatabase db = ClubDatabase.getInstance();

        // --- Создаём пользователей ---
        User aigerim = new User("Айгерим", 20, "Алматы", "telegram", "@aigerim_reads");
        aigerim.addGenre("фэнтези");
        aigerim.addGenre("классика");
        aigerim.addGenre("поэзия");

        User arman = new User("Арман", 22, "Алматы", "email", "arman@mail.kz");
        arman.addGenre("фэнтези");
        arman.addGenre("фантастика");
        arman.addGenre("классика");

        User dana = new User("Дана", 19, "Астана", "sms", "+77010000000");
        dana.addGenre("детектив");
        dana.addGenre("романтика");

        User timur = new User("Тимур", 24, "Алматы", "email", "timur@mail.kz");
        timur.addGenre("фантастика");
        timur.addGenre("классика");

        User viktor = new User("Виктор", 45, "Алматы", "email", "viktor@mail.kz");
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

        // --- 3. Factory: уведомления через разные каналы ---
        System.out.println();
        System.out.println("=== 3. Уведомления о знакомстве (Factory) ===");
        User best = matchmaker.findBest(aigerim, db.getUsers());
        if (best != null) {
            Notifier n1 = NotifierFactory.create(aigerim.getChannel());
            n1.send(aigerim, "Познакомьтесь с " + best.getName() + "!");

            Notifier n2 = NotifierFactory.create(best.getChannel());
            n2.send(best, "Познакомьтесь с " + aigerim.getName() + "!");
        }

        // --- 4. Observer: новости клуба ---
        System.out.println();
        System.out.println("=== 4. Новости клуба (Observer) ===");
        NewsBoard board = new NewsBoard();
        for (User u : db.getUsers()) {
            board.subscribe(u);
        }
        board.publish("Встреча клуба: обсуждаем «Мастер и Маргарита», суббота 18:00");

        board.unsubscribe(dana);
        board.publish("Новая книга месяца: «Маленький принц»");
    }
}

// ======================= МОДЕЛЬ ================================

class User {
    private String name;
    private int age;
    private String city;
    private String channel;   // "email", "telegram" или "sms"
    private String contact;
    private ArrayList<String> genres = new ArrayList<>();

    public User(String name, int age, String city, String channel, String contact) {
        this.name = name;
        this.age = age;
        this.city = city;
        this.channel = channel;
        this.contact = contact;
    }

    public void addGenre(String genre) {
        genres.add(genre);
    }

    public String getName()              { return name; }
    public int getAge()                  { return age; }
    public String getCity()              { return city; }
    public String getChannel()           { return channel; }
    public String getContact()           { return contact; }
    public ArrayList<String> getGenres() { return genres; }

    // Observer: пользователь получает новость
    public void receiveNews(String news) {
        Notifier notifier = NotifierFactory.create(channel);
        notifier.send(this, news);
    }

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

// ======================= 2. FACTORY ============================

interface Notifier {
    void send(User to, String message);
}

class EmailNotifier implements Notifier {
    public void send(User to, String message) {
        System.out.println("  [Email " + to.getContact() + "] " + message);
    }
}

class TelegramNotifier implements Notifier {
    public void send(User to, String message) {
        System.out.println("  [Telegram " + to.getContact() + "] " + message);
    }
}

class SmsNotifier implements Notifier {
    public void send(User to, String message) {
        System.out.println("  [SMS " + to.getContact() + "] " + message);
    }
}

class NotifierFactory {
    public static Notifier create(String channel) {
        if (channel.equals("telegram")) {
            return new TelegramNotifier();
        } else if (channel.equals("sms")) {
            return new SmsNotifier();
        } else {
            return new EmailNotifier();
        }
    }
}

// ======================= 3. STRATEGY ===========================

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

// ======================= 4. OBSERVER ===========================

class NewsBoard {
    private ArrayList<User> subscribers = new ArrayList<>();

    public void subscribe(User user) {
        subscribers.add(user);
    }

    public void unsubscribe(User user) {
        subscribers.remove(user);
        System.out.println(user.getName() + " отписался(ась) от новостей");
    }

    public void publish(String news) {
        System.out.println("Новость: " + news);
        for (User user : subscribers) {
            user.receiveNews(news);
        }
    }
}
