package test.commands;

import com.github.breadmoirai.breadbot.framework.command.MainCommand;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;

import java.util.Deque;

public class MathCommand {

    @MainCommand
    public void math(CommandEvent event, Deque<Double> operands, Deque<Operator> operators) {
        if (operands.size() != operators.size() + 1) {
            event.reply("invalid");
            return;
        } else {
            while (!operators.isEmpty()) {
                Double op1 = operands.pop();
                Double op2 = operands.pop();
                Operator o = operators.pop();
                operands.push(o.calculate(op1, op2));
            }
        }
        event.reply(String.valueOf(operands.pop()));
    }

    public static abstract class Operator {
        public abstract double calculate(double op1, double op2);
    }

    public static class AddOperator extends Operator {

        @Override
        public double calculate(double op1, double op2) {
            return op1 + op2;
        }
    }

    public static class SubtractOperator extends Operator {

        @Override
        public double calculate(double op1, double op2) {
            return op1 - op2;
        }
    }

    public static class MultiplyOperator extends Operator {

        @Override
        public double calculate(double op1, double op2) {
            return op1 * op2;
        }
    }

    public static class DivideOperator extends Operator {

        @Override
        public double calculate(double op1, double op2) {
            return op1 / op2;
        }
    }
}
