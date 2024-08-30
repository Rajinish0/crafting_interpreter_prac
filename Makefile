# Directories
SRC_DIR := lox/src
BIN_DIR := lox/bin


# Find all .java files
SRC_FILES := $(shell find $(SRC_DIR) -name '*.java')

# Main class to run
MAIN_CLASS := lox.Lox

# Compiler
JAVAC := javac
# Java
JAVA := java

# Compile all .java files to .class files
all: $(BIN_DIR)
	$(JAVAC) -d $(BIN_DIR) -sourcepath $(SRC_DIR) $(SRC_FILES)

# Run the main class (this will start the live interpreter)
run: all
	$(JAVA) -cp $(BIN_DIR) $(MAIN_CLASS)

# Clean the build
clean:
	rm -rf $(BIN_DIR)

# Ensure the bin directory exists
$(BIN_DIR):
	@mkdir -p $(BIN_DIR)

.PHONY: all run clean
