import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import javax.mail.*;
import javax.mail.search.FlagTerm;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;


public class Main {
    public static void main(String[] args) throws IOException, MessagingException, InterruptedException {

        // Создаю входящий поток и читаю конфигурации из файла:
        FileInputStream fileInputStream = new FileInputStream("src/main/resources/config.properties");
        Properties properties = new Properties();
        properties.load(fileInputStream);

        // Формирую перменые:
        final String USER = properties.getProperty("mail.user");
        final String PASSWORD = properties.getProperty("mail.password");
        final String HOST = properties.getProperty("mail.host");
        final String TOKEN = properties.getProperty("telegram.token");
        final String CHAT_ID = properties.getProperty("telegram.chat.id");

        // Передаю параметры работы протокола SSL с сервером:
        Properties prop = new Properties();
        prop.put("mail.store.protocol", "imaps");

        // Создаю хранилище сообщений:
        Store store = Session.getInstance(prop).getStore();
        store.connect(HOST, USER, PASSWORD);

        // Передаю боту токен:
        TelegramBot bot = new TelegramBot(TOKEN);

        // Создаю множество для заголовков:
        Set<String> subjects = new HashSet<String>();

        // Формирую флаг непрочитанных писем:
        FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);

        // Создаю массив для непрочитанных писем:
        Message[] messageMassive;

        // Создаю пустую директорию под письма:
        Folder inbox = null;

        while (true) {
            try {
                // Позиционируюсь на вложенной диркториии "jira" и выгружаю письма с возможностью изменения:
                inbox = store.getFolder("INBOX/jira");
                inbox.open(Folder.READ_WRITE); // or Folder.READ_ONLY

                // Заполняю массив непрочитанными письмами:
                messageMassive = inbox.search(ft);

                // Для массива непрочитанных писем:
                for (Message message : messageMassive) {
                    // Заполняю множество уникальными заголовками (отсекаю повторения из вложенных писем):
                    subjects.add(message.getSubject());

                    // Помечаю письмо прочитанным:
                    message.setFlag(Flags.Flag.SEEN, true);

                    /* Тело письма:
                    Multipart multipart = (Multipart) message.getContent();
                    System.out.println(multipart.getContentType());
                    BodyPart body = multipart.getBodyPart(0);
                    System.out.println(body.getContent()); */
                }

                // Для множества уникальных заголовков:
                for (String subject : subjects) {
                    // Отправляю в чат:
                    SendResponse response = bot.execute(new SendMessage(CHAT_ID, subject.toString()));
                }

                /* Общее количество сообщений:
                System.out.println("Всего писем: " + inbox.getMessageCount());
                System.out.println("Непрочитанных писем: " + inbox.getUnreadMessageCount()); */

            } finally {
                // Закрываю сессию работы с директорией:
                inbox.close();

                // Очищаю множество непрочитанных писем:
                subjects.clear();

                // Жду 20 секунд:
                Thread.sleep(20000);
            }
        }
    }
}
