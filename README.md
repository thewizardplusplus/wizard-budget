# Wizard Budget

Программа для ведения личного бюджета.

## Возможности

* ведение списка доходов и расходов;
* поддержка указания тегов для каждой записи;
* автоматический расчет текущего баланса;
* автоматический сбор статистики расходов (с группировкой по тегам);
* парсинг SMS от банка (перехват новых и импорт старых);
* планирование покупок (как разовых, так и ежемесячных);
* анализ отработанных часов в Harvest (расчёт размера недоработок с учётом текущего рабочего календаря);
* ведение списка курсов валют (валюты для наблюдения задаются в настройках);
* виджет с текущим балансом и результатом анализа отработанных часов;
* виджет со списком запланированных покупок;
* виджет со списком курсов наблюдаемых валют.

## Особенности

* использование регулярных выражений, задаваемых пользователем, для парсинга SMS от банка;
* ежедневное автоматическое получение актуального курса валют (через сервис https://www.exchangerate-api.com/);
* поддержка экспорта данных в XML-файл с простой структурой;
* сохранение бекапа данных в Dropbox (в том числе ежедневно автоматически);
* открытый исходный код (репозиторий: https://github.com/thewizardplusplus/wizard-budget).

## Скриншоты

![](docs/screenshots/spendings.png)
![](docs/screenshots/buys.png)
![](docs/screenshots/stats.png)
![](docs/screenshots/hours.png)
![](docs/screenshots/currencies.jpg)

![](docs/screenshots/spendings_widget.jpg)
![](docs/screenshots/spendings_widget_small.jpg)

![](docs/screenshots/buys_widget.jpg)
![](docs/screenshots/buys_widget_only_monthly.jpg)

![](docs/screenshots/currencies_widget_top.jpg)
![](docs/screenshots/currencies_widget_bottom.jpg)

## Лицензия

The MIT License (MIT)

Copyright &copy; 2015, 2023-2024 thewizardplusplus <thewizardplusplus@yandex.ru>
