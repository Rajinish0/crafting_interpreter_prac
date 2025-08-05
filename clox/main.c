#include "common.h"
#include "chunk.h"
#include "debug.h"

int main(int argc, const char* argv[])
{
    Chunk chunk;
    initChunk(&chunk);
	


	int constant = addConstant(&chunk, 1.2);
	//1 byte
	writeChunk(&chunk, OP_CONSTANT, 123);
	//1 byte (int gets down casted to uint8_t)
	writeChunk(&chunk, constant, 123);

    writeChunk(&chunk, OP_RETURN, 123);

    writeChunk(&chunk, OP_CONSTANT_LONG, 123);
    writeConstant(&chunk, 4, 123);

    disassembleChunk(&chunk, "test chunk");
    freeChunk(&chunk);

    return 0;
}
