#include <iostream>
#include <functional>

using namespace std;

template <typename T>
struct Node
{
	T value;
	Node* left;
	Node* right;
};

template <typename T>
class BTree
{
public:
	BTree();
	~BTree();

	void insert(T t);
	Node<T>* search(T t);
	void inOrder(const std::function<void(T&)>& traverseCb);
	void postOrder(const std::function<void(T&)>& traverseCb);
	void preOrder(const std::function<void(T&)>& traverseCb);
	void destroy();

private:
	void insert(T t, Node<T>* leaf);
	Node<T>* search(T t, Node<T>* leaf);
	void inOrder(Node<T>* leaf, const std::function<void(T&)>& traverseCb);
	void postOrder(Node<T>* leaf, const std::function<void(T&)>& traverseCb);
	void preOrder(Node<T>* leaf, const std::function<void(T&)>& traverseCb);
	void destroy(Node<T>* leaf);

	Node<T>* _root;
};

template<typename T>
inline BTree<T>::BTree()
{
	_root = nullptr;
}

template<typename T>
inline BTree<T>::~BTree()
{
	destroy();
}

template<typename T>
inline void BTree<T>::insert(T t)
{
	if (_root != NULL)
	{
		insert(t, _root);
	}
	else
	{
		_root = new Node<T>;
		_root->value = t;
		_root->left = NULL;
		_root->right = NULL;
	}
}

template<typename T>
inline void BTree<T>::insert(T t, Node<T>* leaf)
{
	if (t < leaf->value)
	{
		if (leaf->left != NULL)
		{
			insert(t, leaf->left);
		}
		else
		{
			leaf->left = new Node<T>;
			leaf->left->value = t;
			leaf->left->left = NULL;
			leaf->left->right = NULL;
		}
	}
	else if (t > leaf->value)
	{
		if (leaf->right != NULL)
		{
			insert(t, leaf->right);
		}
		else
		{
			leaf->right = new Node<T>;
			leaf->right->value = t;
			leaf->right->right = NULL;
			leaf->right->left = NULL;
		}
	}
	else
	{
		leaf->value = t;
	}
}

template<typename T>
inline Node<T>* BTree<T>::search(T t)
{
	return search(t, _root);
}

template<typename T>
inline Node<T>* BTree<T>::search(T t, Node<T>* leaf)
{
	if (leaf != nullptr)
	{
		if (t == leaf->value)
		{
			return leaf;
		}
		if (t < leaf->value)
		{
			return search(t, leaf->left);
		}
		else
		{
			return search(t, leaf->right);
		}
	}
	else
	{
		return nullptr;
	}
}

template<typename T>
inline void BTree<T>::inOrder(const std::function<void(T&)>& traverseCb)
{
	inOrder(_root, traverseCb);
}

template<typename T>
inline void BTree<T>::inOrder(Node<T>* leaf, const std::function<void(T&)>& traverseCb)
{
	if (leaf != NULL)
	{
	    inOrder(leaf->left, traverseCb);
		if (traverseCb)
		{
			traverseCb(leaf->value);
		}
		inOrder(leaf->right, traverseCb);
	}
}

template<typename T>
inline void BTree<T>::postOrder(const std::function<void(T&)>& traverseCb)
{
	postOrder(_root, traverseCb);
}

template<typename T>
inline void BTree<T>::postOrder(Node<T>* leaf, const std::function<void(T&)>& traverseCb)
{
	if (leaf != NULL)
	{
	    postOrder(leaf->left, traverseCb);
	    postOrder(leaf->right, traverseCb);
		if (traverseCb)
		{
			traverseCb(leaf->value);
		}
	}
}

template<typename T>
inline void BTree<T>::preOrder(const std::function<void(T&)>& traverseCb)
{
    preOrder(_root, traverseCb);
}

template<typename T>
void BTree<T>::preOrder(Node<T> *leaf, const std::function<void(T&)>& traverseCb) {
    if (leaf != NULL)
    {
        if (traverseCb)
        {
            traverseCb(leaf->value);
        }
        preOrder(leaf->left, traverseCb);
        preOrder(leaf->right, traverseCb);
    }
}

template<typename T>
inline void BTree<T>::destroy()
{
	destroy(_root);
}

template<typename T>
inline void BTree<T>::destroy(Node<T>* leaf)
{
	if (leaf != NULL)
	{
		destroy(leaf->left);
		destroy(leaf->right);
		delete leaf;
	}
}
