#include <stdio.h>

#include "debug.h"
#include "value.h"


void disassembleChunk(Chunk *chunk, const char *name){

    printf("== %s ==\n", name);
	
	printf("Chunk COUNT: %d\n", chunk->count);
    for (int offset =0; offset < chunk->count;){
    offset = disassembleInstruction(chunk, offset);
    }

}

int simpleInstruction(const char *name, int offset)
{
    printf("%s\n", name);
    return offset + 1;
}

int constantInstruction(const char *name, Chunk *chunk, int offset){
	//1 byte is for getting the index of the constant
	// OP_CONSTANT was at offset, the actual idx is at offset+1
	uint8_t constant = chunk->code[offset + 1];
	printf("%-16s %4d '", name, constant);
	// the actual constant can now be obtained from the table using the index
	printValue(chunk->constants.values[constant]);
	printf("'\n");
	
	//consumed 2 bytes
	return offset + 2;
}

int constantLongInstruction(const char *name, Chunk *chunk, int offset){
	uint8_t b1 = chunk->code[offset + 1];
    uint8_t b2 = chunk->code[offset + 2];
    uint8_t b3 = chunk->code[offset + 3];
    int constant = (b1 << (8 * 2) ) | (b2 << 8) | (b3); 
	printf("%-16s %4d '", name, constant);
	// the actual constant can now be obtained from the table using the index
	printValue(chunk->constants.values[constant]);
	printf("'\n");

    printf("DISASSEMBLED IDX %d\n", constant);
	
	//consumed 4 bytes
	return offset + 4;
}

int disassembleInstruction(Chunk *chunk, int offset){
    printf("%04d ", offset);
	// if (offset > 0 && chunk->lines[offset] == chunk->lines[offset - 1]) 
    if (offset > 0 && getLine(chunk, offset) == getLine(chunk, offset - 1)) 
		printf(" |    ");
	else
		// printf("%4d  ", chunk->lines[offset] );
        printf("%d  ", getLine(chunk, offset));
		

    uint8_t instruction = chunk->code[offset];

    switch (instruction){
		case OP_CONSTANT:
			return constantInstruction("OP_CONSTANT", chunk, offset);
        case OP_CONSTANT_LONG:
            return constantLongInstruction("OP_CONSTANT_LONG", chunk, offset);
        case OP_RETURN:
            return simpleInstruction("OP_RETURN", offset);
        default:
            printf("Unknown opcode %d\n", instruction);
            return offset + 1;
    }
}
