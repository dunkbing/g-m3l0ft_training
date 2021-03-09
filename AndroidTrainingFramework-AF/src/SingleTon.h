#pragma once
template <class T>
class SingleTon
{
public:
	SingleTon() {};
	static T* GetInstance()
	{
		if (!s_Instance)
		{
			s_Instance = new T();
		}
		return s_Instance;
	}

private:
	static T* s_Instance;
};

template <class T>
T* SingleTon<T>::s_Instance = 0;