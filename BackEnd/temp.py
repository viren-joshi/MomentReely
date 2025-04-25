import json
import boto3

def lambda_handler(event, context):
    
    dynamodb = boto3.resource('dynamodb')
    table = dynamodb.Table('userData')

    sns = boto3.client('sns')


    for record in event['Records']:
        eventName = record['eventName']
        if eventName == 'INSERT':
            newPost = record['dynamodb']['NewImage']
            postId = newPost['postId']['S']
            userId = newPost['userId']['S']
            receiverId = newPost['receiverId']['S']

            senderName = table.get_item(Key={'userId': userId})['Item']['name']
            receiver = table.get_item(Key={'userId': receiverId})['Item']
            receiverName = receiver['name']
            receiverArn = receiver['notificationData']['arn']

            message = f"Hey! {receiverName} look at the post!"
            title = f"{senderName} sent you a reel!"

            responseData = {}
            try: 
                response = sns.publish(
                    TargetArn=receiverArn,
                    Message=json.dumps({
                        'default': json.dumps(message),
                        'GCM' : json.dumps({
                            'notification': {
                                'title': title,
                                'body': message,
                                'sound': 'default'
                            }
                        })
                    }),
                    MessageStructure='json'
                )

                responseData = {
                    'MessageId': response['MessageId'],
                    'StatusCode': response['ResponseMetadata']['HTTPStatusCode']
                }
            except Exception as e:
                responseData = {
                    'Error': str(e),
                    'StatusCode': 500
                }
            
            return responseData




    return {
        'statusCode': 200,
        'body': json.dumps('Success')
    }


    
