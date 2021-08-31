import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import okhttp3.*;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.ss.usermodel.*;
import java.io.*;
import java.util.*;

import javax.mail.*;
import javax.mail.search.FlagTerm;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws IOException, MessagingException, InterruptedException {

        // Создаю входящий поток и читаю конфигурации из файла:
        FileInputStream fileInputStream = new FileInputStream("src/main/resources/config.properties");
        Properties properties = new Properties();
        properties.load(fileInputStream);

        // Формирую константы:
        final String TOKEN = properties.getProperty("telegram.token");
        final String ADDING_ADDRESS_API = properties.getProperty("mail.api.add");
        final String PASSWORD = properties.getProperty("mail.password");
        final String DOMAINE = properties.getProperty("mail.domaine");
        final String FILE_BD = properties.getProperty("file");;

        // Передаю боту токен:
        TelegramBot bot = new TelegramBot(TOKEN);

        // Получаю все обновления приходящие в бот
        bot.setUpdatesListener(updates -> {

            // Вывожу полученный список данных которые пришли в бота с сообщением
//            updates.forEach(System.out::println);

            // Для каждого из обновлений:
            updates.forEach(update -> {

                        // Если в обновлении содержится сообщение:
                        if (update.message() != null) {
                            String text = update.message().text();

                            // И это сообщение не пустое:
                            if (text != null)

                                // И если сообщение является командой:
                                if (text.startsWith("/createMail")) {  // TODO: добавь проверку для исключения некошерных символов + проверку на команду без username
                                    String[] words = text.split(" ");
                                    String username = words[1].toLowerCase(); // Обязательно перевожу в нижний регистр
                                    try {
                                        // Если такого адреса нет в БД:
                                        if (!ReaderWriter.isExistenceUsername(username, FILE_BD)) {

                                            // Создаю новый аккаунт:
//                                          Postman.AddingNewAddress(username, ADDING_ADDRESS_API);

                                            // Записываю новый адрес в БД:
                                            ReaderWriter.WriteAddressToBD(username, PASSWORD, FILE_BD);
                                            bot.execute(new SendMessage(update.message().chat().id(), "Создал аккаунт с адресом " + username + DOMAINE));
                                        } else {
                                            bot.execute(new SendMessage(update.message().chat().id(), "Этот адрес занят. Выберите другой."));
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                        }
            });
            // Возвращаю статус, что все обновления были прочитанны ботом:
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

        // Временно повешаю тут переменные TODO: убери их
        final String USER = properties.getProperty("mail.user");
        final String HOST = properties.getProperty("mail.host");

        Set<String> subjects = Postman.checkMail(USER, PASSWORD, HOST);
        for (String subject : subjects) { System.out.println(subject.toString()); }
    }
}

class Postman {
    // Статический метод создания почты:
    public static void AddingNewAddress(String username, String urlCreatingAddress) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n    \"username\" : " + username + "\n}\"");
        Request request = new Request.Builder()
                .url(urlCreatingAddress)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
    }

    // Статический метод проверки почты в для пары TODO: Проверь его работу
    public static Set<String> checkMail(String username, String password, String getHost) throws MessagingException {
        String user = username;
        String pass = password;
        String host = getHost;

        // Инициализирую специальный объект Properties типа Hashtable для удобной работы с данными:
        Properties prop = new Properties();
        // Передаю параметры работы протокола SSL с сервером
        prop.put("mail.store.protocol", "imaps");
//        prop.put("mail.store.protocol", "imap");
//        prop.put("mail.imap.starttls.enable", "true");

        // Создаю хранилище сообщений:
        Store store = Session.getInstance(prop).getStore();
        store.connect(host, user, pass);
//        store.connect(HOST, 143, USER, PASSWORD);

        // Создаю множество для заголовков писем:
        Set<String> subjects = new HashSet<String>();

        // Формирую флаг непрочитанных писем:
        FlagTerm unread = new FlagTerm(new Flags(Flags.Flag.SEEN), false);

        // Создаю массив для непрочитанных писем:
        Message[] unreadMessages;

        // Создаю пустую директорию под письма:
        Folder inbox = null;

        try {
            // Позиционируюсь на корневой диркториии "Входящие" и выгружаю письма с возможностью изменения:
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
            }
            // Отправляю в чат множество уникальных заголовков:
            //for (String subject : subjects) { bot.execute(new SendMessage(CHAT_ID, subject.toString())); }

        } finally {
            // Закрываю сессию работы с директорией:
            inbox.close();

            // Жду 20 секунд:
//              Thread.sleep(20000);
            // Очищаю множество непрочитанных писем:
//                subjects.clear();
        }
        return subjects;
    }
}

class ReaderWriter {
    // Статический метод записи адреса в БД:
    public static void WriteAddressToBD(String username, String password, String filename) throws IOException {
        // TODO: Сделай закрытие файла в finally
        try {
            // Создаю входящий поток данных, указываю файл с которым буду работать:
            FileInputStream fileInput = new FileInputStream(filename);

            // Создаю объект для доступа к файлу и нициализирую объект для работы с новой версией excel + задаю параметр:
            XSSFWorkbook workbook = new XSSFWorkbook(fileInput);

            // Создаю объект для доступа к листу. В параметрах указываю с какого листа получать данные:
            XSSFSheet sheet = workbook.getSheetAt(0);

            // Создаю новую строку. Номер новой строки получаю с помощью метода "getLastRowNum() + 1"):
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);

            // Устанавливаю значение для содержимоего 0й и 1й ячеек:
            row.createCell(0).setCellValue(username);
            row.createCell(1).setCellValue(password);

            // Сохраню записанные данные в файл:
            try {
                // Инициализирую объект в который буду записывать данные:
                FileOutputStream fileOut = new FileOutputStream(filename);

                // Передам в нашу excel - книгу как параметр объект этого файла:
                workbook.write(fileOut);

                // Закрываю файл:
                fileOut.close();
            } catch (Exception e) {
                System.out.println("Что - то пошло не так при записи");
            }
        } catch (Exception e) {
            System.out.println("Что - то пошло не так при чтении");
        }
    }

    // Статический метод проверки наличия логина в БД:
    public static boolean isExistenceUsername(String username, String filename) throws IOException {
        // TODO: Сделай закрытие файла в finally

        // Создаю входящий поток данных, указываю файл с которым буду работать:
        FileInputStream file = new FileInputStream(filename);

        // Создаю объект для доступа к файлу и нициализирую объект для работы с новой версией excel + задаю параметр:
        XSSFWorkbook workbook = new XSSFWorkbook(file);

        // Создаю объект для доступа к листу. В параметрах указываю с какого листа получать данные:
        XSSFSheet sheet = workbook.getSheetAt(0);

        // Для всех строк листа:
        for (Row row : sheet) {
            // Считываю значение 0й ячейки:
            Cell cell = row.getCell(0);

            // Получаю из значения строку:
            String value = cell.getStringCellValue();

            // Если значение строки совпадает с создаваемым логином:
            if (username.equals(value)) {
                file.close();
                return true;
            }
        }
        file.close();
        return false;
    }
}

// TODO: Добавление пары chatId : username в .xml
//      + метод чтения пары из листа

// TODO: Проверка всех ящиков по файлу .xml
//      + метод проверки обновлений в новом ящике

// TODO: Если пара не срабатывает, помещаем её false и не работаем с ней (метод проверки отработки и заполнения true/false должен быть отдельным)

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