/*
 * MIT License
 *
 * Copyright (c) 2020 The Mauzo Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.Mauzo.Server.Managers;

import java.sql.SQLException;
import java.util.List;

public interface ManagersIntf<T> {
    /**
     * Clase estatica para gestionar las excepciones que generen
     * las clases que implementen esta interfaz.
     */
    public static class ManagerErrorException extends Exception {
        private static final long serialVersionUID = 4696818002135394231L;
        
        public ManagerErrorException(String msg) {
            super(msg);
        }
    }

    /**
     * Método generico para añadir elementos T a la base de datos.
     * 
     * @param obj   El objeto en cuestión.
     * @throws SQLException Excepción en la base de datos.
     */
    public void add(T obj) throws Exception;

    /**
     * Método generico para obtener elementos a partir del id.
     * 
     * @param id    ID del objeto en la base de datos.
     * @throws SQLException Excepción en la base de datos.
     * @throws ManagerErrorException    Excepción de la clase gestora.
     * 
     * @return El objeto en cuestión.
     */
    public T get(int id) throws Exception;
    
    /**
     * Método generico para obtener un listado de elementos T.
     *
     * @throws SQLException Excepción en la base de datos.
     * @return La lista de elementos T.
     */
    public List<T> getList() throws SQLException;

    /**
     * Método generico para modificar elementos a partir del nombre.
     * 
     * @param obj  El nombre del objeto en la base de datos.
     * @return El el objeto en cuestión.
     * @throws SQLException Excepción en la base de datos.
     * @throws ManagerErrorException    Excepción de la clase gestora.
     */
    public void modify(T obj) throws Exception;
    
    /**
     * Método generico para eliminar elementos a partir del nombre.
     * 
     * @param obj  El nombre del objeto en la base de datos.
     * @throws SQLException Excepción en la base de datos.
     * @throws ManagerErrorException    Excepción de la clase gestora.
     * @return El el objeto en cuestión.
     */
    public void remove(T obj) throws Exception;
}