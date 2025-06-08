package semana12;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

public class Semana10 {
    
    private static final Logger logger = LoggerFactory.getLogger(Semana10.class);
    
    public static void main(String[] args) {
        logger.info("PROBANDO LAS 4 LIBRERIAS!");
        
        // LOGBACK
        logger.error(" Logback ERROR funciona");
        logger.warn(" Logback WARN funciona");
        logger.info(" Logback INFO funciona");
        
        // GUAVA
        var lista = Lists.newArrayList("Juan", "Maria", "Carlos");
        logger.info(" Guava creo lista: {}", lista);
        
        // APACHE COMMONS
        String nombre = StringUtils.capitalize("juan perez");
        logger.info(" Commons limpio nombre: {}", nombre);
        
        logger.info(" TODAS LAS LIBRERIAS FUNCIONAN!");
    }
}