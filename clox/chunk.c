#include <stdio.h>
#include <stdlib.h>

#include "chunk.h"


void initChunk(Chunk *chunk)
{
    chunk->count =0;
    chunk->capacity =0;
    chunk->code =NULL;
    chunk->lineCnt =0;
    chunk->lineCapacity =0;
	chunk->lines =NULL;
	initValueArray(&chunk->constants);
}

void writeChunk(Chunk *chunk, uint8_t byte, int line)
{
    if (chunk->capacity < chunk->count + 1) {
        int oldCapacity = chunk->capacity;
        chunk->capacity = GROW_CAPACITY(oldCapacity);
        chunk->code = GROW_ARRAY(uint8_t, chunk->code, oldCapacity, chunk->capacity);
	}

    chunk->code[chunk->count] = byte;
    chunk->count++;
    printf("CHUNK COUNT NOW: %d\n", chunk->count);

    // basic run length encoding
    // why + 2? Because on adding a new element, i add the count and then the element
    // ideally the capacity should only grow when we actually want to grow the array
    // but i reckon it's okay this way too for now.
    if (chunk->lineCapacity < chunk->lineCnt + 2){
        int oldCapacity = chunk->lineCapacity;
        chunk->lineCapacity = GROW_CAPACITY(oldCapacity);
        chunk->lines = GROW_ARRAY(int, chunk->lines, oldCapacity, chunk->lineCapacity);
    }


    if (chunk->lineCnt == 0 || 
        line != chunk->lineCnt - 1){
        chunk->lines[chunk->lineCnt] = 1;
        chunk->lines[chunk->lineCnt + 1] = line;
        chunk->lineCnt += 2;
    } else {
        chunk->lines[chunk->lineCnt - 2]++;
    }

}

int writeConstant(Chunk *chunk, Value value, int line){
    writeValueArray(&chunk->constants, value);
    int idx = (chunk->constants.count - 1);
    writeChunk(chunk, (idx>>(8 * 2))&0xff, line);
    writeChunk(chunk, (idx>>(8 * 1))&0xff, line);
    writeChunk(chunk, (idx>>(8 * 0))&0xff, line);
    printf("writing %d", idx);
    return idx;
}

int addConstant(Chunk *chunk, Value constant){
	writeValueArray(&chunk->constants, constant);
	//return idx of insertion
	return chunk->constants.count - 1;
}

int getLine(Chunk *chunk, int idx){
    int cnt =chunk->lines[0];
    int cInd =2;
    while (idx > cnt && cInd < chunk->lineCnt) {
        cnt += chunk->lines[cInd];
        cInd += 2;
    }

    if (cInd >= chunk->lineCnt) return -1;
    return chunk->lines[cInd - 1];
}


void freeChunk(Chunk *chunk)
{
    FREE_ARRAY(uint8_t, chunk->code, chunk->capacity);
	FREE_ARRAY(int, chunk->lines, chunk->capacity);
	freeValueArray(&chunk->constants);
    initChunk(chunk);
}
