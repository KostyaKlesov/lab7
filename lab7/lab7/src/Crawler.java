import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.net.*;
import java.io.*;


// Веб-парсер
public class Crawler {
    private static final String URL_PREFIX = "http://";
    private static final String URL_HREF = "<a href=";
    private static final String ERROR = "Usage: java Crawler <URL> <depth>";
    public static void main(String[] args){
        if (args.length!=2) { // проверка на входные данные. Если их не 2, то ошибка
            System.out.println(ERROR);
            return;
        }
        String url = args[0];
        int maxDepth; // максимальная глубина
        try {
            maxDepth = Integer.parseInt(args[1]); //пытаемся преобразовать строку к числу
        } catch (NumberFormatException e){
            System.out.println(ERROR);
            return;
        }
        if (maxDepth<=0){
            System.out.println(ERROR);
            return;
        }
        // LinkedList - как массив в питоне, только для одного типа данных. Нефиксированная длина.
        LinkedList<URLDepthPair> open = new LinkedList<>(); // создаем список для необработанных ссылок
        LinkedList<URLDepthPair> close = new LinkedList<>(); // создаем список для обработанных ссылок
        LinkedList<URLDepthPair> temp = new LinkedList<>();
        URLDepthPair firstpair;
        try {
            firstpair = new URLDepthPair(url, 0); // пытаемся создать первую пару. Если ссылка битая - ошибка
        } catch (MalformedURLException e) {
            System.out.println(ERROR);
            return;
        }
        open.add(firstpair); // добавлем первую ссылку в список необработанных
        while (open.size()!= 0){ // цикл выполняется пока все ссылки не обработаются
            URLDepthPair pair = open.removeFirst(); // получаем и удаляем ссылку из необработанных
            if (pair.getDepth()>maxDepth) continue; // если длина необработой строки больше максимальной, то мы ее не рассматриваем
            temp = read(pair); // находим новые ссылки и добавляем их к необработанным
            open.addAll(temp);
            close.add(pair); // добавляем старую ссылку в список обработанных
        }
        for (URLDepthPair pair: close){
            System.out.println(pair); // выводим обработанные ссылки.
        }
    }
    public static LinkedList<URLDepthPair> read(URLDepthPair pair){
        Socket socket;
        try { //выполнгяем для каждой ссылки
            socket = new Socket(pair.urlObj.getHost(), 80); // создаем сокет. Порт 80 отвечает за http/tcp трафик
        } catch (UnknownHostException e) {
            System.out.println("Неизвестный хост "+ pair);
            return null;
        } catch (IOException e) {
            System.out.println("Ошибка ввода/вывода "+ pair);
            return null;
        }
        try {
            socket.setSoTimeout(1000); // таймаут в милисеккундах
        } catch (SocketException e) {
            System.out.println("Ошибка установки таймаута "+ pair); // пытаемся обратиться к буфферами системы
            return null; // пытемся секкунду. Если не получается, то функция завершается
        }
        BufferedReader in; // описываем потоки ввода/вывода
        PrintWriter out;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // создаем потоки ввода/вывода
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Ошибка создания потоков ввода/вывода "+ pair);
            return null;
        }
        // Начало обращения к серверу
        out.println("GET " + pair.urlObj.getPath() + " HTTP/1.1"); // отправляем стороку. Переводит в последовательность байт
        out.println("Host: " + pair.urlObj.getHost());
        out.println("Connection: close");
        out.println();
        // конец обращения к серверу
        String temp;
        LinkedList<URLDepthPair> pairs = new LinkedList<>();
        try {
            while ((temp = in.readLine())!=null){ // построчно считываем документ. Пытаемся считать html документ и ищем в нем ссылкт
                int idx = temp.indexOf(URL_PREFIX);
                int endIdx = temp.indexOf("\"", idx+1);
                if (!temp.contains(URL_HREF) || idx == -1 || endIdx==-1)continue;
                URLDepthPair newPair = null;
                try {
                    newPair = new URLDepthPair(temp.substring(idx, endIdx), pair.getDepth()+1);
                } catch (MalformedURLException e) {

                }
                pairs.add(newPair); // если нашли ссылку, то добавляем в список ссылок
            }
        } catch (IOException e) {
            System.out.println("Ошибка считывания "+ pair);
            return null;
        }
        try {
            socket.close(); // закрываем сокет
        } catch (IOException e) {
            System.out.println("Ошибка закрытия соккета "+ pair);
            return null;
        }
        return pairs;
    }
}
