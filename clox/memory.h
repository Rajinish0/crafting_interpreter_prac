#ifndef clox_memory_h
#define clox_memory_h

#define GROW_CAPACITY(x) \
        ((x) < 8 ? 8 : (x) * 2)

#define GROW_ARRAY(type, pointer, oldCount, newCount) (type*)reallocate(pointer, sizeof(type) * (oldCount), sizeof(type) * (newCount))


#define FREE_ARRAY(type, pointer, oldCount) reallocate(pointer, sizeof(type) * (oldCount), 0)

void* reallocate(void *ptr, size_t oldSize, size_t newSize);


#endif
