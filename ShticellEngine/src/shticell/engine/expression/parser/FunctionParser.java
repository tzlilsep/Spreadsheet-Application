package shticell.engine.expression.parser;

import shticell.engine.cell.api.EffectiveValue;
import shticell.engine.coordinate.Coordinate;
import shticell.engine.coordinate.CoordinateFactory;
import shticell.engine.expression.api.Expression;
import shticell.engine.expression.api.impl.*;
import shticell.engine.cell.api.CellType;
import shticell.engine.sheet.impl.SheetImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public enum FunctionParser {
    IDENTITY {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly one argument
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Invalid number of arguments for IDENTITY function. Expected 1, but got " + arguments.size());
            }

            // all is good. create the relevant function instance
            String actualValue = arguments.get(0);
            if (isBoolean(actualValue)) {
                return new IdentityExpression(Boolean.parseBoolean(actualValue), CellType.BOOLEAN);
            } else if (isNumeric(actualValue)) {
                return new IdentityExpression(Double.parseDouble(actualValue), CellType.NUMERIC);
            } else {
                return new IdentityExpression(actualValue, CellType.STRING);
            }
        }

        private boolean isBoolean(String value) {
            return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
        }

        private boolean isNumeric(String value) {
            try {
                Double.parseDouble(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    },
    PLUS {
        @Override
        public Expression parse(List<String> arguments) {
            // Validate that we have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for PLUS function. Expected 2, but got " + arguments.size());
            }

            // Parse the left and right expressions
            Expression left = FunctionParser.parseExpression(arguments.get(0).trim(), null, null);
            Expression right = FunctionParser.parseExpression(arguments.get(1).trim(), null, null);


            // Create and return the PlusExpression
            return new PlusExpression(left, right);
        }
    },
    MINUS {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for MINUS function. Expected 2, but got " + arguments.size());
            }

            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0).trim(),null,null);
            Expression right = parseExpression(arguments.get(1).trim(),null,null);



            // all is good. create the relevant function instance
            return new MinusExpression(left, right);
        }
    },
    UPPER_CASE {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly one argument
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Invalid number of arguments for UPPER_CASE function. Expected 1, but got " + arguments.size());
            }

            // structure is good. parse arguments
            Expression arg = parseExpression(arguments.get(0),null,null);

            // more validations on the expected argument types
            if (!arg.getFunctionResultType().equals(CellType.STRING)) {
                throw new IllegalArgumentException("Invalid argument types for UPPER_CASE function. Expected STRING, but got " + arg.getFunctionResultType());
            }

            // all is good. create the relevant function instance
            return new UpperCaseExpression(arg);
        }
    },
    CONCAT {
        @Override
        public Expression parse(List<String> arguments) {

            // Validate that we have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for CONCAT function. Expected 2, but got " + arguments.size());
            }

            // Parse the left and right expressions
            Expression left = parseExpression(arguments.get(0), null, null);
            Expression right = parseExpression(arguments.get(1), null, null);

            // Create and return the ConcatExpression
            return new ConcatExpression(left, right);
        }
    },
    REF {
        @Override
        public Expression parse(List<String> arguments) {
            // Validate that we have exactly one argument
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Invalid number of arguments for REF function. Expected 1, but got " + arguments.size());
            }


            // Create and return a ReferenceExpression instance using the cell-id
            String cellId = arguments.get(0).trim();
            return new ReferenceExpression(cellId);
        }
    } ,POW {
        @Override
        public Expression parse(List<String> arguments) {
            // Validate that we have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for POW function. Expected 2, but got " + arguments.size());
            }

            // Parse the base and exponent expressions
            Expression base = parseExpression(arguments.get(0).trim(), null, null);
            Expression exponent = parseExpression(arguments.get(1).trim(), null, null);


            // Create and return the PowExpression
            return new PowExpression(base, exponent);
        }
    }, TIMES {
        @Override
        public Expression parse(List<String> arguments) {
            // Validate that we have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for TIMES function. Expected 2, but got " + arguments.size());
            }

            // Parse the left and right expressions
            Expression left = parseExpression(arguments.get(0).trim(), null, null);
            Expression right = parseExpression(arguments.get(1).trim(), null, null);


            // Create and return the TimesExpression
            return new TimesExpression(left, right);
        }
    },DIVIDE {
        @Override
        public Expression parse(List<String> arguments) {
            // Validate that we have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for DIVIDE function. Expected 2, but got " + arguments.size());
            }

            // Parse the numerator and denominator expressions
            Expression numerator = parseExpression(arguments.get(0).trim(), null, null);
            Expression denominator = parseExpression(arguments.get(1).trim(), null, null);


            // Create and return the DivideExpression
            return new DivideExpression(numerator, denominator);
        }


    },MOD {
        @Override
        public Expression parse(List<String> arguments) {
            // Validate that we have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for MOD function. Expected 2, but got " + arguments.size());
            }

            // Parse the left and right expressions
            Expression left = parseExpression(arguments.get(0).trim(), null, null);
            Expression right = parseExpression(arguments.get(1).trim(), null, null);

            // Create and return the ModExpression
            return new ModExpression(left, right);
        }
    },ABS {
        @Override
        public Expression parse(List<String> arguments) {
            // Validate that we have exactly one argument
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Invalid number of arguments for ABS function. Expected 1, but got " + arguments.size());
            }

            // Parse the argument expression
            Expression argument = parseExpression(arguments.get(0).trim(), null, null);


            // Create and return the AbsExpression
            return new AbsExpression(argument);
        }
    } ,SUB {
        @Override
        public Expression parse(List<String> arguments) {
            // Validate that we have exactly three arguments
            if (arguments.size() != 3) {
                throw new IllegalArgumentException("Invalid number of arguments for SUB function. Expected 3, but got " + arguments.size());
            }

            // Parse the source string, start index, and end index expressions
            Expression source = parseExpression(arguments.get(0).trim(), null, null);
            Expression startIndex = parseExpression(arguments.get(1).trim(), null, null);
            Expression endIndex = parseExpression(arguments.get(2).trim(), null, null);

            // Create and return the SubExpression
            return new SubExpression(source, startIndex, endIndex);
        }
    }, PERCENT {
        @Override
        public Expression parse(List<String> arguments) {
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for PERCENT function. Expected 2, but got " + arguments.size());
            }

            Expression part = FunctionParser.parseExpression(arguments.get(0).trim(), null, null);
            Expression whole = FunctionParser.parseExpression(arguments.get(1).trim(), null, null);

            return new PercentExpression(part, whole);
        }
    },
    AND {
        @Override
        public Expression parse(List<String> arguments) {
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for AND function. Expected 2, but got " + arguments.size());
            }

            Expression arg1 = FunctionParser.parseExpression(arguments.get(0).trim(), null, null);
            Expression arg2 = FunctionParser.parseExpression(arguments.get(1).trim(), null, null);

            return new AndExpression(arg1, arg2);
        }
    },
    OR {
        @Override
        public Expression parse(List<String> arguments) {
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for OR function. Expected 2, but got " + arguments.size());
            }

            Expression arg1 = FunctionParser.parseExpression(arguments.get(0).trim(), null, null);
            Expression arg2 = FunctionParser.parseExpression(arguments.get(1).trim(), null, null);

            return new OrExpression(arg1, arg2);
        }
    },
    LESS {
        @Override
        public Expression parse(List<String> arguments) {
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for LESS function. Expected 2, but got " + arguments.size());
            }

            Expression arg1 = FunctionParser.parseExpression(arguments.get(0).trim(), null, null);
            Expression arg2 = FunctionParser.parseExpression(arguments.get(1).trim(), null, null);

            return new LessExpression(arg1, arg2);
        }
    },
    BIGGER {
        @Override
        public Expression parse(List<String> arguments) {
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for BIGGER function. Expected 2, but got " + arguments.size());
            }

            Expression arg1 = FunctionParser.parseExpression(arguments.get(0).trim(), null, null);
            Expression arg2 = FunctionParser.parseExpression(arguments.get(1).trim(), null, null);

            return new BiggerExpression(arg1, arg2);
        }
    },
    NOT {
        @Override
        public Expression parse(List<String> arguments) {
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Invalid number of arguments for NOT function. Expected 1, but got " + arguments.size());
            }

            Expression exp = FunctionParser.parseExpression(arguments.get(0).trim(), null, null);

            return new NotExpression(exp);
        }
    }, SUM {
        @Override
        public Expression parse(List<String> arguments) {
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Invalid number of arguments for SUM function. Expected 1, but got " + arguments.size());
            }

            String rangeName = arguments.get(0).trim();
            return new SumExpression(rangeName);
        }
    },
    AVERAGE {
        @Override
        public Expression parse(List<String> arguments) {
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Invalid number of arguments for AVERAGE function. Expected 1, but got " + arguments.size());
            }

            String rangeName = arguments.get(0).trim();
            return new AverageExpression(rangeName);
        }
    }, IF {
        @Override
        public Expression parse(List<String> arguments) {
            if (arguments.size() != 3) {
                throw new IllegalArgumentException("Invalid number of arguments for IF function. Expected 3, but got " + arguments.size());
            }

            Expression condition = FunctionParser.parseExpression(arguments.get(0).trim(), null, null);
            Expression thenExpr = FunctionParser.parseExpression(arguments.get(1).trim(), null, null);
            Expression elseExpr = FunctionParser.parseExpression(arguments.get(2).trim(), null, null);

            return new IfExpression(condition, thenExpr, elseExpr);
        }
    },
    EQUAL {
        @Override
        public Expression parse(List<String> arguments) {
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for EQUAL function. Expected 2, but got " + arguments.size());
            }

            Expression arg1 = FunctionParser.parseExpression(arguments.get(0).trim(), null, null);
            Expression arg2 = FunctionParser.parseExpression(arguments.get(1).trim(), null, null);

            return new EqualExpression(arg1, arg2);
        }
    }

    ;



    abstract public Expression parse(List<String> arguments);

    public static Expression parseExpression(String input, Coordinate coordinate, SheetImpl sheet) {
        if (input.startsWith("{") && input.endsWith("}")) {
            String functionContent = input.substring(1, input.length() - 1);
            List<String> topLevelParts = parseMainParts(functionContent);

            String functionName = topLevelParts.get(0).trim().toUpperCase();

            topLevelParts.remove(0);

            try
            {
                return FunctionParser.valueOf(functionName).parse(topLevelParts);
            }
            catch (Exception e)
            {
                throw new IllegalArgumentException("Function "+functionName+" does not exist");
            }
        }

        return FunctionParser.IDENTITY.parse(List.of(input));
    }

    private static List<String> parseMainParts(String input) {
        List<String> parts = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        Stack<Character> stack = new Stack<>();

        for (char c : input.toCharArray()) {
            if (c == '{') {
                stack.push(c);
            } else if (c == '}') {
                if (!stack.isEmpty()) {
                    stack.pop();
                } else {
                    // Log or throw an error for unmatched '}'
                    System.out.println("Unmatched closing brace '}' in input: " + input);
                    throw new IllegalArgumentException("Unmatched closing brace '}' in input: " + input);
                }
            }

            if (c == ',' && stack.isEmpty()) {
                // If we are at a comma and the stack is empty, it's a separator for top-level parts
                parts.add(buffer.toString());
                buffer.setLength(0); // Clear the buffer for the next part
            } else {
                buffer.append(c);
            }
        }

        // Add the last part
        if (buffer.length() > 0) {
            parts.add(buffer.toString());
        }

        // Check for unmatched opening braces
        if (!stack.isEmpty()) {
            System.out.println("Unmatched opening brace '{' in input: " + input);
            throw new IllegalArgumentException("Unmatched opening brace '{' in input: " + input);
        }

        return parts;
    }



    public static Coordinate parseCellId(String cellId) {
        try {
            String columnPart = cellId.replaceAll("\\d", ""); // Extract alphabetic part (column)
            String rowPart = cellId.replaceAll("\\D", ""); // Extract numeric part (row)

            int row = Integer.parseInt(rowPart) - 1; // Convert to zero-based index
            int column = columnPart.toUpperCase().charAt(0) - 'A'; // Convert 'A' to 0, 'B' to 1, etc.

            // Use CoordinateFactory to create or retrieve the Coordinate instance
            return CoordinateFactory.createCoordinate(row, column);
        } catch (Exception e) {
            return null; // Return null if parsing fails
        }
    }


}