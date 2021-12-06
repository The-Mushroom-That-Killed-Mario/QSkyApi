/*
 *  Copyright (C) 2010 {Apertum}Projects. web: www.apertum.ru email: info@apertum.ru
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ru.apertum.qsky.common;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Этот класс исключения использовать для програмной генерации исклюсений.
 * Записывает StackTrace и  само исключение в лог.
 * Это исключение не показывает диологовое окно при возникновении ошибки
 * Используется в системе статистики и отчетов.
 *
 * @author Evgeniy Egorov
 */
public class ServerException extends RuntimeException {

    private Logger log = LogManager.getLogger(ServerException.class);

    public ServerException(String textException) {
        super(textException);
        log.error("Error! " + textException, this);
    }

    public ServerException(Exception ex) {
        super(ex);
        log.error("Error! " + ex.toString(), this);
    }

    public ServerException(String textException, Exception ex) {
        super(textException, ex);
        log.error("Error! " + textException + "\n" + ex.toString(), this);
    }
}
