#include <assert.h>
#include <string.h>
#include <sys/types.h>
#include <unistd.h>


typedef struct block_meta{
	size_t size;
	struct block_meta *next;
	int free; //0, 1
	int magic; // for debugging, not for padding!
} block_meta;

#define META_SIZE sizeof(block_meta)
#define REQ_MAGIC 0x12345678
#define MALLOC_MAGIC 0x77777777
#define FREE_MAGIC 0x55555555

void *global_base =NULL;

block_meta *find_free_block(block_meta **last, size_t size){
	block_meta *current = global_base;
	while (current && !(current->free && current->size > size)){
		*last = current;
		current = current->next;
	}
	return current;
}


block_meta *request_space(block_meta *last, size_t size){
	block_meta *block;
	block = sbrk(0);
	void *req = sbrk(size + META_SIZE);

	assert((void*)block == req);
	if (req == (void*)-1) return NULL;


	if (last) {
		last->next = block;
	}
	
	block->size  = size;
	block->next  = NULL;
	block->free  = 0;
	block->magic = REQ_MAGIC;

	return block;
}

/* gunky old malloc
void *malloc(size_t size){
	void *p = sbrk(0); //current top
	void *req = sbrk(size);

	if (req == (void*)-1){
		return NULL;
	}
	else {
		//sbrk returns the previous head
		assert (p == req);
		return p;
	}
}
*/

void *malloc(size_t size){
	block_meta *block;
	if (size <= 0) return NULL;
	
	if (!global_base){
		block = request_space(NULL, size);
		if (!block) return NULL;
		global_base = block;
	} else {
		block_meta *last = global_base;
		block = find_free_block(&last, size);
		if (!block) {
			block = request_space(last, size);
			if (!block) return NULL;
		} else {
			block->free = 0;
			block->magic = MALLOC_MAGIC;
		}
	}

	//convenient way of doing block + META_SIZE
	// or block + sizeof(block), since block is a ptr
	return (block + 1);
}

block_meta *get_block_ptr(void *ptr){
	if (!ptr) return NULL;

	return (block_meta*)(ptr) - 1;
}

void free(void *ptr){
	if (!ptr) return;
	
	block_meta *block = get_block_ptr(ptr);
	assert(block->free == 0);
	assert(block->magic == REQ_MAGIC || 
		   block->magic == MALLOC_MAGIC);

	block->free = 1;
	block->magic = FREE_MAGIC;
}

void *realloc(void *ptr, size_t size){
	if (!ptr) {
		return malloc(size);
	}
	
	block_meta *block = get_block_ptr(ptr);
	if (block->size >= size) {
		return ptr;
	} 
	

	void *new_mem;
	new_mem = malloc(size);
	if (!new_mem) return NULL;

	memcpy(new_mem, ptr, block->size);
	free(ptr);
	return new_mem;
}

void *calloc(size_t num, size_t size){
	size_t total_size = num * size;
	void *ptr = malloc(total_size);
	/*
	for (int *i =ptr; i < (char*)ptr + (total_size); ++i)
		*i = 0;
	*/
	memset(ptr, 0, total_size);
	return ptr;
}


/*int main(){
}*/
