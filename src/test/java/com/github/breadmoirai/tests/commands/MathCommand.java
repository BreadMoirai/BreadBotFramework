/*
 *        Copyright 2017 Ton Ly (BreadMoirai)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.github.breadmoirai.tests.commands;

import com.github.breadmoirai.breadbot.framework.annotation.command.Description;
import com.github.breadmoirai.breadbot.framework.annotation.command.MainCommand;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;

import java.util.Deque;

public class MathCommand {

    @MainCommand
    @Description("This command can only use the 4 basic operators and evaluates expressions left to right disregarding any order of operations")
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
