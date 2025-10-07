#!/bin/bash
# Guideon Regulation Search System - Run Script

# Check if GOOGLE_API_KEY is set
if [ -z "$GOOGLE_API_KEY" ]; then
    echo "Error: GOOGLE_API_KEY environment variable is not set."
    echo ""
    echo "Please set your Google Gemini API key:"
    echo "  export GOOGLE_API_KEY=your_api_key_here"
    echo ""
    exit 1
fi

echo "Starting Guideon Regulation Search System..."
echo "Using Java 17..."
echo ""

"C:\Program Files\Java\jdk-17\bin\java" -jar target/regulation-search-1.0.0.jar
