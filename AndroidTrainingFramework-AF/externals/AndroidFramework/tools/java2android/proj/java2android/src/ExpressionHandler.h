#pragma once

#include <string>
#include <vector>
#include <map>

class ExpressionHandler
{
	ExpressionHandler();
	ExpressionHandler(const ExpressionHandler&);
	ExpressionHandler operator=(const ExpressionHandler&);

	static std::map<std::string, bool> EmptyBoolTable;
	static std::map<std::string, bool> &boolTable;

	struct Token
	{
		struct Type
		{
			enum type { Variable, Not, And, Or, OpenParanthesis, CloseParanthesis, COUNT, INVALID };
		private:
			Type();
			Type(const Type&);
			Type operator=(const Type&);
		};
		struct OperatorType
		{
			enum type { NotAnOperator, Unary, Binary, Other, COUNT };
		private:
			OperatorType();
			OperatorType(const OperatorType&);
			OperatorType operator=(const OperatorType&);
		};

		static const OperatorType::type operatorType[Type::COUNT];
		static const std::string symbol[Type::COUNT];
		static const size_t precedence[Type::COUNT];

		Type::type type;
		std::string value;
	};
public:
	static std::map<std::string, bool>& BoolTable();
	static void BoolTable(std::map<std::string, bool>& table);
	static void ResetBoolTable();
	
private:
	static bool evaluateBoolVariable(const std::string &name);
	static bool evaluateBoolUnaryOperation(Token::Type::type op, bool var);
	static bool evaluateBoolBinaryOperation(Token::Type::type op, bool var1, bool var2);
	static bool evaluateBoolTokenExpression(std::vector<Token> &tokens);
	static void extractBoolTokenExpression(const std::string &expression, std::vector<Token> &tokens);
public:
	static bool EvaluateBoolExpression(const std::string &expression, bool &isWellformed);
};
