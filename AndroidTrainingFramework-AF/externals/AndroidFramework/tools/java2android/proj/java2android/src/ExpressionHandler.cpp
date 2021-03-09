#include "ExpressionHandler.h"

std::map<std::string, bool> ExpressionHandler::EmptyBoolTable = std::map<std::string, bool>();
std::map<std::string, bool>& ExpressionHandler::boolTable = ExpressionHandler::EmptyBoolTable;

const ExpressionHandler::Token::OperatorType::type ExpressionHandler::Token::operatorType[Type::COUNT] = { OperatorType::NotAnOperator, OperatorType::Unary, OperatorType::Binary, OperatorType::Binary, OperatorType::Other, OperatorType::Other };
const std::string ExpressionHandler::Token::symbol[Type::COUNT] = { "", "!", "&&", "||", "(", ")" };
const size_t ExpressionHandler::Token::precedence[Type::COUNT] = { 100000, 3, 2, 1, 0, 0 };

std::map<std::string, bool>& ExpressionHandler::BoolTable()
{
	return boolTable;
}
void ExpressionHandler::BoolTable(std::map<std::string, bool>& table)
{
	boolTable = table;
}

void ExpressionHandler::ResetBoolTable()
{
	boolTable = EmptyBoolTable;
}

bool ExpressionHandler::evaluateBoolVariable(const std::string &name)
{
	std::map<std::string, bool>::const_iterator i = boolTable.find(name);
	if (i != boolTable.end())
	{
		return i->second;
	}
	else
	{
		return false;
	}
}

bool ExpressionHandler::evaluateBoolUnaryOperation(Token::Type::type op, bool var)
{
	switch (op)
	{
	case Token::Type::Not:
	{
		return !var;
	}
	break;
	default:
	{
		return false;
	}
	break;
	}
}

bool ExpressionHandler::evaluateBoolBinaryOperation(Token::Type::type op, bool var1, bool var2)
{
	switch (op)
	{
	case Token::Type::And:
	{
		return var1 && var2;
	}
	break;
	case Token::Type::Or:
	{
		return var1 || var2;
	}
	break;
	default:
	{
		return false;
	}
	break;
	}
}

bool ExpressionHandler::evaluateBoolTokenExpression(std::vector<Token> &tokens)
{
	std::vector<bool> variables;
	std::vector<Token::Type::type> operators;
	size_t vpos = 0;
	size_t opos = 0;
	size_t crtpos = 0;
	size_t maxpos = tokens.size();

	while (crtpos < maxpos)
	{
		Token &token = tokens[crtpos];
		crtpos++;
		if (token.type == Token::Type::Variable)
		{
			bool value = evaluateBoolVariable(token.value);
			if (vpos == variables.size())
			{
				variables.push_back(false);
			}
			variables[vpos] = value;
			vpos++;
		}
		else
		{
			if (token.type != Token::Type::OpenParanthesis)
			{
				bool stop = false;
				while (Token::precedence[operators[opos - 1]] >= Token::precedence[token.type] && !(Token::operatorType[operators[opos - 1]] == Token::OperatorType::Unary && Token::operatorType[token.type] == Token::OperatorType::Unary))
				{
					Token::Type::type op = operators[opos - 1];
					opos--;

					switch (Token::operatorType[op])
					{
					case Token::OperatorType::Unary:
					{
						bool var = variables[vpos - 1];
						vpos--;

						bool value = evaluateBoolUnaryOperation(op, var);

						variables[vpos] = value;
						vpos++;
					}
					break;
					case Token::OperatorType::Binary:
					{
						bool var2 = variables[vpos - 1];
						vpos--;
						bool var1 = variables[vpos - 1];
						vpos--;

						bool value = evaluateBoolBinaryOperation(op, var1, var2);

						variables[vpos] = value;
						vpos++;
					}
					break;
					case Token::OperatorType::Other:
					{
						stop = true;
					}
					break;
					default:
					{
						
					}
					break;
					}
					if (stop)
					{
						break;
					}
				}
			}
			if (token.type != Token::Type::CloseParanthesis)
			{
				if (opos == operators.size())
				{
					operators.push_back(Token::Type::INVALID);
				}
				operators[opos] = token.type;
				opos++;
			}
		}
	}

	return variables[0];
}

void ExpressionHandler::extractBoolTokenExpression(const std::string &expression, std::vector<Token> &tokens)
{
	//std::vector<Token> tokens;
	tokens.clear();
	size_t crtpos = 0;
	size_t maxpos = expression.size();

	while (crtpos < maxpos)
	{
		size_t nxtpos = maxpos;
		Token::Type::type nxtop = Token::Type::INVALID;
		for (int i = 1; i < Token::Type::COUNT; i++)
		{
			size_t newpos = expression.find(Token::symbol[i], crtpos);
			newpos = newpos == expression.npos ? maxpos : newpos;
			if (nxtpos > newpos)
			{
				nxtpos = newpos;
				nxtop = (Token::Type::type)(Token::Type::Variable + i);
			}
		}
		if (nxtpos == maxpos)
		{
			Token var;
			var.type = Token::Type::Variable;
			var.value = expression.substr(crtpos, nxtpos - crtpos);
			tokens.push_back(var);

			crtpos = nxtpos;
		}
		else
		{
			if (crtpos != nxtpos)
			{
				Token var;
				var.type = Token::Type::Variable;
				var.value = expression.substr(crtpos, nxtpos - crtpos);
				tokens.push_back(var);

				crtpos = nxtpos;
			}
			Token op;
			op.type = nxtop;
			op.value = "";
			tokens.push_back(op);

			crtpos += (Token::symbol[nxtop]).size();
		}
	}
}

bool ExpressionHandler::EvaluateBoolExpression(const std::string &expression, bool &isWellformed)
{
	isWellformed = true;
	if (expression.size() == 0)
	{
		return true;
	}

	if (expression.find(" ", 0) != expression.npos || expression.find("\t", 0) != expression.npos)
	{
		isWellformed = false;
		return false;
	}

	std::vector<Token> tokens;
	extractBoolTokenExpression(expression, tokens);

	int paranthesiscount = 0;
	bool wellformed = true;
	if (!(tokens[0].type == Token::Type::Variable || tokens.size() > 1 && (tokens[0].type == Token::Type::OpenParanthesis || Token::operatorType[tokens[0].type] == Token::OperatorType::Unary)))
	{
		wellformed = false;
	}
	if (tokens[0].type == Token::Type::OpenParanthesis)
	{
		paranthesiscount++;
	}
	if (wellformed && tokens.size() > 1)
	{
		std::vector<Token>::iterator prv = tokens.begin();
		std::vector<Token>::iterator crt = prv + 1;
		for (; crt != tokens.end() && wellformed && paranthesiscount >= 0; prv++, crt++)
		{
			switch (crt->type)
			{
			case Token::Type::Variable:
			{
				switch (prv->type)
				{
				case Token::Type::Not:
				case Token::Type::And:
				case Token::Type::Or:
				case Token::Type::OpenParanthesis:
				break;
				default:
				{
					wellformed = false;
				}
				break;
				}
			}
			break;
			case Token::Type::Not:
			{
				switch (prv->type)
				{
				case Token::Type::Not:
				case Token::Type::And:
				case Token::Type::Or:
				case Token::Type::OpenParanthesis:
				break;
				default:
				{
					wellformed = false;
				}
				break;
				}
			}
			break;
			case Token::Type::And:
			{
				switch (prv->type)
				{
				case Token::Type::Variable:
				case Token::Type::CloseParanthesis:
				break;
				default:
				{
					wellformed = false;
				}
				break;
				}
			}
			break;
			case Token::Type::Or:
			{
				switch (prv->type)
				{
				case Token::Type::Variable:
				case Token::Type::CloseParanthesis:
				break;
				default:
				{
					wellformed = false;
				}
				break;
				}
			}
			break;
			case Token::Type::OpenParanthesis:
			{
				paranthesiscount++;
				switch (prv->type)
				{
				case Token::Type::Not:
				case Token::Type::And:
				case Token::Type::Or:
				case Token::Type::OpenParanthesis:
				break;
				default:
				{
					wellformed = false;
				}
				break;
				}
			}
			break;
			case Token::Type::CloseParanthesis:
			{
				paranthesiscount--;
				switch (prv->type)
				{
				case Token::Type::Variable:
				case Token::Type::CloseParanthesis:
				break;
				default:
				{
					wellformed = false;
				}
				break;
				}
			}
			break;
			default:
			{
				wellformed = false;
			}
			break;
			}
		}
	}
	if (!wellformed || paranthesiscount != 0)
	{
		isWellformed = false;
		return false;
	}

	Token padding;
	padding.type = Token::Type::INVALID;
	padding.value = "";
	tokens.push_back(padding);

	Token cp;
	cp.type = Token::Type::CloseParanthesis;
	cp.value = "";
	tokens.push_back(cp);

	for (int i = tokens.size() - 2; i >= 1; i--)
	{
		tokens[i].type = tokens[i - 1].type;
		tokens[i].value = tokens[i - 1].value;
	}

	tokens[0].type = Token::Type::OpenParanthesis;
	tokens[0].value = "";


	return evaluateBoolTokenExpression(tokens);
}
