import java.util.ArrayList;
import java.util.List;

// BookLoversClub: AFTER refactoring with the Observer pattern.
// The club (subject) does not know about concrete channels.
// Each channel is an observer that subscribes itself.
// To add a new channel we only add a new class; BookClub does not change.

// Observer interface
interface BookObserver {
    void update(String message);
}

// Concrete observers
class EmailObserver implements BookObserver {
    public void update(String message) {
        System.out.println("Email: " + message);
    }
}

class SmsObserver implements BookObserver {
    public void update(String message) {
        System.out.println("SMS: " + message);
    }
}

class TelegramObserver implements BookObserver {
    public void update(String message) {
        System.out.println("Telegram: " + message);
    }
}

// Subject
class BookClub {
    private final List<BookObserver> observers = new ArrayList<>();

    public void subscribe(BookObserver observer) {
        observers.add(observer);
    }

    public void unsubscribe(BookObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(String message) {
        for (BookObserver observer : observers) {
            observer.update(message);
        }
    }

    public void addBook(String title) {
        notifyObservers("New book added: " + title);
    }
}

public class Main {
    public static void main(String[] args) {
        BookClub club = new BookClub();
        club.subscribe(new EmailObserver());
        club.subscribe(new SmsObserver());
        club.subscribe(new TelegramObserver());

        club.addBook("Clean Code");
    }
}
