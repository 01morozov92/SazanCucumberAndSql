# language: ru
# [author: Morozov Ilya]
@PskToPdnCalculation
Функциональность: RKK_OKR

  Предыстория: Тестирование расчета ПСК для ПДН. Метод GET_TOTAL_LOAN_PRICE MSP-T108 (1.0)
    * Заполнить шаблон значениями
      | waveId                | 0 |
      | batchId               | 0 |
      | packId                | 0 |
      | customerNum           | 0 |
      | customerId            | 0 |
      | customerId2           | 0 |
      | customerId3           | 0 |
      | loanPriceAmt          | 0 |
      | AGR_TYPE_CD           | 0 |
      | LOAN_TYPE_CD          | 0 |
      | LIABILITY_CURRENCY_CD | 0 |
      | INITIAL_LOAN_RUB_AMT  | 0 |
      | openDate              | 0 |
      | closeDate             | 0 |
      | TOTAL_LOAN_PRICE_AMT  | 0 |

  Сценарий: Инсерт данных
    * Выполнить SQL-запрос sql/insertWave.sql в БД MYSQL_DB и сохранить значение ячейки в переменную waveId
    * Послать HTTP запрос в эндпоинт https://randomuser.me/api/ c дефолтными заголовками
    * Сгенерировать переменную responseVar по ответному http-сообщению
    * Проверить содержится ли gender в json-теле http-сообщения responseVar