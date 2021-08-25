import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;
import okhttp3.*;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.ss.usermodel.*;
import java.io.*;
import java.util.*;

import javax.mail.MessagingException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws IOException, MessagingException, InterruptedException {

        // Создаю входящий поток и читаю конфигурации из файла:
        FileInputStream fileInputStream = new FileInputStream("src/main/resources/config.properties");
        Properties properties = new Properties();
        properties.load(fileInputStream);

        // Формирую константу:
        final String TOKEN = properties.getProperty("telegram.token");

        final String ADDING_ADDRESS_API = properties.getProperty("mail.api.add");

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
                            try {
                                Postman.AddingNewAddress(username, ADDING_ADDRESS_API); // Создаю новый аккаунт
                                bot.execute(new SendMessage(update.message().chat().id(), "СОЗДАЛ ПОЧТУ С АДРЕСОМ: " + username + "@email2tg.ru"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                }
            });
            // Возвращаю статус, что все обновления были прочитанны ботом:
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });


        // ЧТЕНИЕ: -------------------------- TODO: Сделай закрытие файла в finally
        try{
            FileInputStream file = new FileInputStream(new File("/Volumes/DB RRA/2_dev/Java/MailToTelegram/src/main/resources/base.xlsx"));
            // Инициализирую объект для работы с новой версией excel и задаю параметр:
            XSSFWorkbook workbookNew = new XSSFWorkbook(file); // Это я сделал объект для доступа к файлу
            // То же самое делаю для листа с индексом 0:
            XSSFSheet sheetNew = workbookNew.getSheetAt(0); // Указал с какого листа буду получать данные
            // Получаю количество строк:
            System.out.println(sheetNew.getLastRowNum());
            // Пройдусь по всем строкам с помощью итератора:
            Iterator<Row> rowIterator = sheetNew.iterator(); // Row - это интерфейс библиотеки usermodel
            // Чтобы пройтись по всем строкам нашего файла воспользуемся циклом while:
            while (rowIterator.hasNext()) { // Пока есть данные в нашем файле:
                Row row = rowIterator.next(); // Создаю объект интерфейса row и присваиваю позицию первой строке нашего файла
                // Чтобы перебирать содержимое ячеек файла тоже воспользуемся итератором,и но уже для ячеек:
                Iterator<Cell> cellIterator = row.cellIterator();
                // Реализую цикл в котором буду перебирать ячейки построчно:
                while (cellIterator.hasNext()) { // До тех пор пока есть заполненные ячейки
                    Cell cell = cellIterator.next(); // Создаю объект интерфейса Cell и присваиваю ему данные из ячейки
                    // С помощью оператора switch буду определять какие типы данных считываю из ячейки:
                    switch (cell.getCellType()) { // Получаю тип данных в ячейке
                        case NUMERIC: // Если числовой
                            System.out.println(cell.getNumericCellValue()); // Вывожу это значение
                            break;
                        case STRING: // Если тип текстовый
                            System.out.print(cell.getStringCellValue() + "\t\t"); // Вывожу текстовое значние + два tab
                            break;
                    }
                }
                System.out.println(); // перехожу на новую строку:
            }
            file.close(); // Закрываю файл
        } catch (Exception e) {
            System.out.println("Что - то пошло не так");
        }
/*
        // ЗАПИСЬ: --------------------------- TODO: Сделай закрытие файла в finally
        // Создаю книгу:
        Workbook workbook = new XSSFWorkbook();
        // Создаю лист
        Sheet newSheet = workbook.createSheet("base");
        // Создаю строку с указанием её номера:
        Row row = newSheet.createRow(0);
        // Создаю 0ю ячейку и записываю в неё данные:
        row.createCell(0).setCellValue("Username");
        // Создаю 1ю ячейку и записываю в неё данные:
        row.createCell(1).setCellValue("Password");
//
        // Создаю строку с указанием её номера:
        Row row1 = newSheet.createRow(1);
        // Создаю 0ю ячейку и записываю в неё данные:
        row1.createCell(0).setCellValue("User2");
        // Создаю 1ю ячейку и записываю в неё данные:
        row1.createCell(1).setCellValue("Pass2");

        // Сохраню записанные данные в файл:
        try{
            // Инициализирую объект в который буду записывать данные:
            FileOutputStream fileOut = new FileOutputStream("/Volumes/DB RRA/2_dev/Java/MailToTelegram/src/main/resources/base.xlsx");
            // Передам в нашу excel - книгу как параметр объект этого файла:
            workbook.write(fileOut);
            // Закрываю файл:
            fileOut.close();
            System.out.println("Файл создан");
        }
        catch (Exception e) {
            System.out.println("Что - то пошло не так(");
        }
*/

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

    // Статический метод проверки почты в для пары
}

class ReaderWriter {
    // Статический метод записи пары
    // Статический метод чтения пары
    // Статический метод подсчёта количества пар в базе
}

// TODO: Добавление пары chatId : username в .xml
//      + метод получения количества строк на листе
//      + метод чтения пары из листа
//      + метод записи новой пары в конец листа (если такой пары не сущуествует)
//
// TODO: Проверка всех ящиков по файлу .xml
//      + метод проверки обновлений в новом ящике

// TODO: Если пара не срабатывает, помещаем её false и не работаем с ней

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