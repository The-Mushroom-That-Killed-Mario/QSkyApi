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
package ru.apertum.qsky.api;

import javax.ejb.Local;

/**
 * @author egorov
 */
@Local
public interface ICustomerEvents {

    void changeCustomerStatus(Long branchId, Long serviceId, Long employeeId, Long customerId, Integer status, Integer number, String prefix);

    void insertCustomer(Long branchId, Long serviceId, Long customerId, Long beforeCustId, Long afterCustId);

    void removeCustomer(Long branchId, Long serviceId, Long customerId);

    Integer ping(String version);

    void sendServiceName(Long branchId, Long serviceId, String name);

    void sendUserName(Long branchId, Long employeeId, String name);
}
