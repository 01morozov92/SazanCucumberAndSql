# language: ru
# [author: Morozov Ilya]
@PskToPdnCalculation
Функциональность: RKK_OKR

  Предыстория: Тестирование расчета ПСК для ПДН. Метод GET_TOTAL_LOAN_PRICE MSP-T108 (1.0)
    * Заполнить шаблон значениями
      | waveId                | 0     |
      | batchId               | 0     |
      | packId                | 0     |
      | customerNum           | male |

  Сценарий: Инсерт данных
    * Выполнить SQL-запрос sql/GetLastNameByActorId.sql в БД MYSQL_DB и сохранить значение ячейки в переменную waveId
    * Послать HTTP запрос в эндпоинт https://randomuser.me/api/ c дефолтными заголовками
    * Сгенерировать переменную responseVar по ответному http-сообщению
    * вывести в консоль переменную responseVar
    * Сохранить results:gender из json-тела сообщения responseVar в переменную param
    * вывести в консоль переменную param
    * вывести в консоль переменную customerNum
    * Изменить значение переменной customerNum на ${param}
    * вывести в консоль переменную customerNum

  Сценарий: Инсерт данных
    * вывести в консоль переменную param
    * вывести в консоль переменную param
    * Убедиться в истинности выражения param == customerNum