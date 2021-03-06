package com.github.breadmoirai.breadbot.framework.command;

import com.github.breadmoirai.breadbot.framework.parameter.CommandParameter;

import java.lang.reflect.Method;
import java.util.List;

public interface CommandHandle extends Command {

    List<CommandPreprocessor> getPreprocessors();

    CommandPropertyMap getPropertyMap();

    /**
     * This method returns null if the command has been defined with a class or a supplier.
     * If this command was defined with a Consumer or Object, it will return that object.
     *
     * @return the object supplied to create this command.
     */
    Object getDeclaringObject();

    /**
     * This command returns the enclosing class of this command.
     * <ul>
     * <li>If this command was defined by a Consumer, the Class of that consumer is returned.</li>
     * <li>If this command was defined by a Supplier, the Class of the result from that Supplier is returned.</li>
     * <li>If this command was defined by an Object, the Class of that Object is returned.</li>
     * <li>If this command was defined by a Class, then that Class is returned.</li>
     * <li>If this command is a sub-command defined by a Method, then the Class or Inner Class enclosing that Method is returned.</li>
     * </ul>
     *
     * @return a Class.
     */
    Class getDeclaringClass();

    /**
     * Returns the method that is used to invoke this command.
     * If this command was defined with a Consumer, this returns {@code null}.
     *
     * @return a Method
     */
    Method getDeclaringMethod();

    /**
     * Creates a copy of the backing array and returns the parameters of this Command
     *
     * @return an array of CommandParameters
     */
    CommandParameter[] getParameters();
}
