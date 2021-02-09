//Paso 1. Importar los paquetes requeridos
import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class SQL {
    // url del driver JDBC y la base de datos
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost/Comida";
    // usuario y contraseña de la base de datos
    static final String USER = "root";
    static final String PASS = "";

    public static Scanner teclado = new Scanner(System.in);

    static Connection conn = null;
    static Statement stmt = null;

    public static void main(String[] args) {

        try{
            //Paso 2: Cargar driver JDBC
            Class.forName("com.mysql.jdbc.Driver");
            //Paso 3: Abrir una conexion
            System.out.println("Conectando a la base de datos...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            menu();

            //Paso 6: Limpiar
            //Paso 6: Limpiar
            stmt.close();
            conn.close();
        }catch(SQLException se){
            //Errores de jdbc
            se.printStackTrace();
        }catch(Exception e){
            //Errores de Class.forName
            e.printStackTrace();
        }finally{
            //bloque usado para cerrar recursos
            try{
                if(stmt!=null)
                    stmt.close();
            }catch(SQLException se2){
            }// nada que hacer
            try{
                if(conn!=null)
                    conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        System.out.println("Adios!");
    }

    public static void menu() throws SQLException {

        int eleccion = -1;

        do {
            System.out.println("\nBienvenido a la base de datos");

            System.out.println("Escoja una opción:");
            System.out.println("1 - Ver pizzas");
            System.out.println("2 - Eliminar una pizza");
            System.out.println("3 - Insertar una pizza");
            System.out.println("4 - Editar una pizza");
            System.out.println("0 - Salir");
            System.out.print("Seleccione una opción: ");
            eleccion = teclado.nextInt();
            if(teclado.hasNextLine()) teclado.nextLine();//Borrar buffer scanner (causa estragos)

            switch(eleccion) {
                case 1:
                    mostrarPizzas();
                    break;
                case 2:
                    eliminarPizza();
                    break;
                case 3:
                    insertarNuevaPìzza();
                    break;
                case 4:
                    editarPizza();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opción no válida");
            }

        } while(eleccion!=0);

    }

    public static void mostrarPizzas() throws SQLException{

        ResultSet rs = conn.prepareStatement("SELECT * FROM Pizza").executeQuery();
        System.out.println("Las pizzas disponibles son: ");
        //Paso 5: Extraer datos del resultado
        while(rs.next()){
            //Recuperar por el nombre de la columna
            int codigoPizza = rs.getInt("codigoPizza");
            int valorPizza = rs.getInt("valorPizza");
            String nombrePizza = rs.getString("nombrePizza");
            //Mostar resultados
            System.out.print("Codigo: " + codigoPizza);
            System.out.print(", Valor: " + valorPizza);
            System.out.println(", Nombre: " + nombrePizza);

        }


    }
    public static boolean pizzaExiste(int codigoPizza) throws SQLException{

        return consultar("SELECT * FROM pizza WHERE codigoPizza="+codigoPizza).next();

    }
    public static void eliminarPizza() throws SQLException{
        PreparedStatement pstmDelete = conn.prepareStatement("DELETE FROM pizza WHERE codigoPizza = ?");
        int codigoPizza;
        while (true){
            System.out.println("Ingrese el código de la pizza a eliminar: ");
            codigoPizza = teclado.nextInt();
            if(teclado.hasNextLine()) teclado.nextLine(); //Borrar buffer scanner (causa estragos)

            if (pizzaExiste(codigoPizza)){
                System.out.println("La pizza a eliminar es: ");
                consultarUnaPizza(codigoPizza);
                System.out.println("¿Esta seguro de eliminar esta pizza? (Y para Si, Otro para No)");
                String confirmacion = teclado.nextLine();
                if(confirmacion.equalsIgnoreCase("Y")){
                    pstmDelete.setInt(1,codigoPizza);
                    pstmDelete.executeUpdate();
                    System.out.println("La pizza se ha eliminado correctamente");
                    break;
                }else {
                    System.out.println("Operación abortada: la pizza no se ha eliminado");
                }

            }else {
                System.err.println("El código de la pizza ingresada no existe");
            }

        }

    }
    private static void editarPizza() throws SQLException{

        PreparedStatement pstm = conn.prepareStatement("UPDATE pizza SET  valorPizza=?, nombrePizza=? WHERE codigoPizza=?");
        int codigoPizza;
        int valorPizza;
        String nombrePizza;
        while (true) {

            System.out.print("Ingrese el codigo de la pizza a modificar: ");

            try {
                codigoPizza = teclado.nextInt();
                if(teclado.hasNextLine()) teclado.nextLine(); //Borrar buffer scanner (causa estragos)


                if(pizzaExiste(codigoPizza)){//El codigo existe
                    //Mostramos que pizza modificaremos (Antiguos datos)
                    System.out.println("La pizza a modificar es: ");
                    consultarUnaPizza(codigoPizza);

                    System.out.println("Ingrese el nuevo Nombre: ");
                    nombrePizza = teclado.nextLine();
                    System.out.println("Ingrese el nuevo Precio: ");
                    valorPizza = teclado.nextInt();
                    if (valorPizza > 0) {
                        pstm.setInt(1,valorPizza);
                        pstm.setString(2,nombrePizza);
                        pstm.setInt(3,codigoPizza);
                        pstm.executeUpdate();
                        System.out.println("Se ha ejecutado correctamente el update");
                        break;


                    }else{
                        System.err.println("Error: el valor de la pizza debe ser mayor que 0");
                    }



                }else{// El codigo no existe
                    System.err.println("Error: el codigo ingresado no pertenece a ninguna pizza");
                }
            } catch (InputMismatchException exception) {
                System.err.println("Error, usted no ha ingresado un numero");
            }
        }
    }

    public static void insertarNuevaPìzza() throws SQLException{
        ResultSet rs = consultar("SELECT MAX(codigoPizza) FROM pizza");
        int codigoPizza=0;
        if (rs.next()) codigoPizza = rs.getInt(1) + 1;
        System.out.println("Ingrese una nueva Pizza, codigo de la nueva pizza: "+codigoPizza);
        System.out.print("Ingrese el nombre de la Pizza: ");
        String nombrePizza = teclado.nextLine();
        System.out.print("Ingrese el precio de la Pizza: ");
        int precioPizza = teclado.nextInt();
        String sqlNuevaPizza = "INSERT INTO Pizza VALUES ("+codigoPizza+","+precioPizza+",\'"+nombrePizza+"\');";
        insertar(sqlNuevaPizza);
        System.out.println("Se ha insertado la pizza");

    }

    public static void consultarUnaPizza(int codigoPizza) throws SQLException{
        PreparedStatement pstm = conn.prepareStatement("SELECT * FROM pizza WHERE codigoPizza = ?");
        pstm.setInt(1,codigoPizza);
        ResultSet rs = pstm.executeQuery();
        rs.next();
        //ResultSet rs = consultar("SELECT * FROM pizza WHERE codigoPizza="+codigoPizza);
        //codigoPizza = rs.getInt("codigoPizza");
        int valorPizza = rs.getInt("valorPizza");
        String nombrePizza = rs.getString("nombrePizza");
        //Mostar resultados
        System.out.print("Codigo: " + codigoPizza);
        System.out.print(", Valor: " + valorPizza);
        System.out.println(", Nombre: " + nombrePizza);

    }
    public static ResultSet consultar(String sql) throws SQLException {
        //Paso 4: Ejecutar una consulta
        //System.out.println("Creando declaracion...");
        stmt = conn.createStatement();
        return stmt.executeQuery(sql);
    }

    public static void insertar(String sql)throws SQLException{
        stmt.executeUpdate(sql);
    }



}