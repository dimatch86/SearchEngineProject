# SearchEngine
---
Spring-MVC приложение, позволяющее обходить все страницы заданных сайтов и индексировать их так, чтобы потом находить наиболее релевантные страницы по любому поисковому запросу.

## Стек используемых технологий:
- SpringBoot
- Hibernate
- MySQL
- Java - libs:
  - Jsoup
  - LuceneMorfology
  - Commons - io
  - Lombok
---
## Структура проекта
Проект с подключенными библиотеками лемматизаторами. Содержит несколько контроллеров, сервисов и репозиториев с подключением к бд MySQL.

В части индексации порядок классов бизнес-логики следующий:
![pic1](pic1.jpg)
В классе IndexManager содержатся методы запуска, остановки, добавления отдельной страницы и сбора статистики, класс NetParser непосредственно обрабатывает страницы с использованием технологии ForkJoin.

В части обработки поискового запроса порядок классов аналогичный:
![pic2](pic2.jpg)
В классе SearchController  происходит выбор метода поиска по одному сайту или по всем, в классе SearchManager реализованы эти методы, в классе QueryProcessor непосредственно поиск. Ключевая технология здесь представлена нативным SQL-запросом:

```java
@Query(value = "SELECT * FROM page p " +
            "JOIN `index` i ON p.id = i.page_id " +
            "JOIN lemma l ON l.id = i.lemma_id " +
            "JOIN site s ON s.id = p.site_id " +
            "WHERE l.lemma IN (:lemmasList) " +
            "AND s.id = :id " +
            "GROUP BY p.path " +
            "HAVING COUNT(*) = :count " +
            "ORDER BY SUM(i.rank) DESC", nativeQuery = true)
    List<Page> findPagesByNativeQueryWithSiteId(@Param("lemmasList") Set<String> lemmasList,
                                      @Param("count") int count,
                                                @Param("id") int siteId);

```
---
## Запуск и настройка
