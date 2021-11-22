CREATE TABLE money_currency
(
    money_currency_name   VARCHAR(3) UNIQUE NOT NULL,
    money_currency_symbol VARCHAR(5) UNIQUE NOT NULL,
    PRIMARY KEY (money_currency_name)
);