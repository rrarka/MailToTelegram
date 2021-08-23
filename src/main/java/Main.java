import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;

import javax.mail.*;
import javax.mail.search.FlagTerm;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException, MessagingException, InterruptedException {

        // Создаю входящий поток и читаю конфигурации из файла:
        FileInputStream fileInputStream = new FileInputStream("src/main/resources/config.properties");
        Properties properties = new Properties();
        properties.load(fileInputStream);

        // Формирую константу:
        final String TOKEN = properties.getProperty("telegram.token");
        // Передаю боту токен:
        TelegramBot bot = new TelegramBot(TOKEN);

        // Мап хранит базу чатов и создан ли для этого чата почтовый ящик (true/false):
        Map<Long, Boolean> chats = new HashMap<>();

        // Получаю все обновления приходящие в бот
        bot.setUpdatesListener(updates -> {
//            updates.forEach(System.out::println); // Вывожу полученный список данных которые пришли в бота с сообщением
            // Для каждого из обновлений:
            updates.forEach(update -> {
                // Если в обновлении содержится сообщение:
                if (update.message() != null){
                    String text = update.message().text();
                    // И это сообщение не пустое:
                    if (text != null)
                        // И если сообщение является командой:
                        if (text.startsWith("/createMail")) {  // TODO: добавь проверку для исключения некошерных символов + проверку на команду без username
                            String[] words = text.split(" ");
                            String username = words[1].toLowerCase(); // Обязательно перевожу в нижний регистр
                            bot.execute(new SendMessage(update.message().chat().id(), "СОЗДАЮ ПОЧТУ С АДРЕСОМ: " + username + "@email2tg.ru"));
                    }
                }
            });
            // Возвращаю статус, что все обновления были прочитанны ботом:
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}

// TODO: Статический метод создания почты
// TODO: Добавление пары chatId : username в .xml
// TODO: Проверка всех ящиков по файлу .xml

/*
//         Формирую константы:
        final String USER = properties.getProperty("mail.user");
        final String PASSWORD = properties.getProperty("mail.password");
        final String HOST = properties.getProperty("mail.host");
        final String CHAT_ID = properties.getProperty("telegram.chat.id");

        // Инициализирую специальный объект Properties типа Hashtable для удобной работы с данными:
        Properties prop = new Properties(); // Передаю параметры работы протокола SSL с сервером
        prop.put("mail.store.protocol", "imaps");

//        prop.put("mail.store.protocol", "imap");
//        prop.put("mail.imap.starttls.enable", "true");

        // Создаю хранилище сообщений:
        Store store = Session.getInstance(prop).getStore();
        store.connect(HOST, USER, PASSWORD);
//        store.connect(HOST, 143,USER, PASSWORD);


        // Создаю множество для заголовков писем:
        Set<String> subjects = new HashSet<String>();

        // Формирую флаг непрочитанных писем:
        FlagTerm unread = new FlagTerm(new Flags(Flags.Flag.SEEN), false);

        // Создаю массив для непрочитанных писем:
        Message[] unreadMessages;

        // Создаю пустую директорию под письма:
        Folder inbox = null;

        while (true) {
            try {
                // Позиционируюсь на вложенной диркториии "jira" и выгружаю письма с возможностью изменения:
//                inbox = store.getFolder("INBOX/jira");
                inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_WRITE); // or Folder.READ_ONLY

                // Заполняю массив непрочитанными письмами:
                unreadMessages = inbox.search(unread);

                // Для массива непрочитанных писем:
                for (Message message : unreadMessages) {
                    // Заполняю множество уникальными заголовками (отсекаю повторения из вложенных писем):
                    subjects.add(message.getSubject());

                    // Помечаю письмо прочитанным:
                    message.setFlag(Flags.Flag.SEEN, true);

                    /* Тело письма:
                    Multipart multipart = (Multipart) message.getContent();
                    System.out.println(multipart.getContentType());
                    BodyPart body = multipart.getBodyPart(0);
                    System.out.println(body.getContent()); */
        /*
                }

                // Отправляю в чат множество уникальных заголовков:
                for (String subject : subjects) { bot.execute(new SendMessage(CHAT_ID, subject.toString())); }

                /* Общее количество сообщений:
                System.out.println("Всего писем: " + inbox.getMessageCount());
                System.out.println("Непрочитанных писем: " + inbox.getUnreadMessageCount()); */
        /*

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
*/