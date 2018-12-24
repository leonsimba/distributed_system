/*
 * An example for hiredis sync API.
 *
 * Date     : 2018-12-24
 * Version  : redis-4.0.12
 * Author   : zhiping he
 *
 * gcc -o sync_demo sync_demo.c -L /usr/local/lib -lhiredis
 * export LD_LIBRARY_PATH=/usr/local/lib
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <hiredis/hiredis.h>

void parse_reply(redisReply *reply)
{
	int seq = 0;

	if (!reply) {
		printf("redisReply is NULL\n");
		return;
	}

	switch (reply->type) {
		case REDIS_REPLY_STATUS:
			printf("REDIS_REPLY_STATUS: %s\n", reply->str);
			break;
		case REDIS_REPLY_ERROR:
			printf("REDIS_REPLY_ERROR: %s\n", reply->str);
			break;
		case REDIS_REPLY_INTEGER:
			printf("REDIS_REPLY_INTEGER: %lld\n", reply->integer);
			break;
		case REDIS_REPLY_NIL:
			printf("REDIS_REPLY_NIL:NULL\n");
			break;
		case REDIS_REPLY_STRING:
			printf("REDIS_REPLY_STRING: %s\n", reply->str);
			break;
		case REDIS_REPLY_ARRAY:
			printf("REDIS_REPLY_ARRAY\n");
			while(seq < reply->elements) {
				printf("\treply->elements[%d]: %s\n", seq, reply->element[seq]->str);
				seq++;
			}
			break;
		default:
			break;
	}
	freeReplyObject(reply);
}

// RPUSH mykey value1 value2 value3
void complex_cmd_test(redisContext *c)
{
	int i;
	int argc = 5;
	const char *argv[5] = {"RPUSH", "mykey", "value1", "value2", "value3"};
	size_t argvlen[5];
	redisReply *reply;

	for (i = 0; i < argc; i++) {
		argvlen[i] = strlen(argv[i]);
	}

	// delete list
	reply = redisCommand(c, "DEL mykey 0 -1");
	parse_reply(reply);

	// insert a list
	reply = redisCommandArgv(c, argc, argv, argvlen);
	parse_reply(reply);

	// get the list
	reply = redisCommand(c,"LRANGE mykey 0 -1");
	parse_reply(reply);
}

void simple_cmd_test(redisContext *c)
{
	redisReply *reply;

	// PING server
	reply = redisCommand(c,"PING");
	parse_reply(reply);

	// Set a key
	reply = redisCommand(c,"SET %s %s", "foo", "hello world");
	parse_reply(reply);

	// Set a key using binary safe API
	reply = redisCommand(c,"SET %b %b", "bar", (size_t) 3, "hello", (size_t) 5);
	parse_reply(reply);
}

int main(int argc, char **argv)
{
	redisContext *c;
	int port = 6379;
	const char *hostname = "127.0.0.1";
	struct timeval timeout = { 1, 500000 }; // 1.5 seconds

	// Step: create a connection with Redis Server
	c = redisConnectWithTimeout(hostname, port, timeout);
	if (c == NULL || c->err) {
	    if (c) {
	        printf("Connection error: %s\n", c->errstr);
	        redisFree(c);
	    } else {
	        printf("Connection error: can't allocate redis context\n");
	    }
	    exit(1);
	}

	// Step: send some simple commands to Redis Server
	simple_cmd_test(c);

	// Step: send some simple commands to Redis Server
	complex_cmd_test(c);

	// Step: disconnects and frees the context
	redisFree(c);

	return 0;
}
