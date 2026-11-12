@echo off
chcp 65001 > nul

set DB_PASSWORD=gru_password

echo Подключаемся к Oracle и выполняем скрипт...
docker exec -i gru_db sqlplus GRU/%DB_PASSWORD%@//localhost:1521/XEPDB1 < fill_GRU_VISTA_TAB.sql

echo.
pause
