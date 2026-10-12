import java.util.ArrayList;
import java.util.List;

// BookLoversClub: BEFORE refactoring.
// Problem: the switch grows every time we add a new communication channel.
// To add a channel we must edit addBook() (violates Open/Closed Principle).

class BookClub {
    private final List<String> channels = new ArrayList<>();

    public void subscribe(String channel) {
        channels.add(channel);
    }

    public void addBook(String title) {
        String message = "New book added: " + title;
        for (String channel : channels) {
            switch (channel) {
                case "email":
                    System.out.println("Email: " + message);
                    break;
                case "sms":
                    System.out.println("SMS: " + message);
                    break;
                case "telegram":
                    System.out.println("Telegram: " + message);
                    break;
                // every new channel = another case here
                default:
                    throw new IllegalArgumentException("Unknown channel: " + channel);
            }
        }
    }
}

public class Main {
    public static void main(String[] args) {
        BookClub club = new BookClub();
        club.subscribe("email");
        club.subscribe("sms");
        club.subscribe("telegram");

        club.addBook("Clean Code");
    }
}
