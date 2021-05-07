/**
 * 
 */
package com.strandls.activity.pojo;

import java.sql.Types;

import org.hibernate.dialect.PostgreSQLDialect;

/**
 * @author Abhishek Rudra
 *
 * 
 */
@SuppressWarnings("deprecation")
public class PostgreSQL10JsonDialect extends PostgreSQLDialect {
  
    public PostgreSQL10JsonDialect() {
        super();
        this.registerColumnType(Types.JAVA_OBJECT, "json");
    }
}