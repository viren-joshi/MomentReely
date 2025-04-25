from flask import Flask, request, jsonify
from functools import wraps
import firebase_admin
from firebase_admin import credentials, auth
import boto3
from boto3.dynamodb.conditions import Attr
import base64
import io
import uuid
from werkzeug.utils import secure_filename
from datetime import datetime

# Use `create-json` GitHub action to create the JSON file.
cred = credentials.Certificate("firebase-credentials.json")
firebase_admin.initialize_app(cred)
s3 = boto3.client('s3')
bucket_name = 'momentreely-bucket-iac'

dynamo = boto3.resource('dynamodb', region_name='us-east-1')
userTable = dynamo.Table('userData-iac')
postsTable = dynamo.Table('reels-iac')

sns = boto3.client('sns', region_name='us-east-1')
platform_application_arn = 'arn:aws:sns:us-east-1:200002507379:app/GCM/momentreely'

app = Flask(__name__)

def firebase_auth_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        # Check for Authorization header
        auth_header = request.headers.get('Authorization')
        if not auth_header:
            return jsonify({"error": "Authorization header missing"}), 401

        # Bearer token format
        id_token = auth_header

        try:
            # Verify the token
            decoded_token = auth.verify_id_token(id_token)
            request.user = decoded_token  # Attach user info to request if needed
            
        except Exception as e:
            print(e)
            return jsonify({"error": f"Invalid token: {str(e)}"}), 401

        return f(*args, **kwargs)

    return decorated_function

@app.route('/health', methods=['GET'])
def health_check():
    return jsonify({"status": "ok"}), 200

@app.route('/registerUserDevice', methods=['POST'])
@firebase_auth_required
def register_user_device():
    data = request.get_json()
    user_id = data.get('userId')
    device_id = data.get('deviceId')

    if not user_id or not device_id:
        return jsonify({"success": False, "message": "User Id or Device Id null"}), 400

    try:
        # Register the device with Amazon SNS
        response = sns.create_platform_endpoint(
            PlatformApplicationArn=platform_application_arn,
            Token=device_id,
            CustomUserData=user_id
        )
        endpoint_arn = response['EndpointArn']

        # Update the user record in DynamoDB
        response = userTable.update_item(
            Key={'userId': user_id},
            UpdateExpression='SET notificationData.arn = :newArn, notificationData.deviceId = :newDeviceId',
            ExpressionAttributeValues={
                ':newArn': endpoint_arn,
                ':newDeviceId': device_id
            },
            ReturnValues='UPDATED_NEW'
        )
        return jsonify({"success": True, "message": "Device registered successfully", "data": response}), 200

    except Exception as e:
        if "EndpointAlreadyExists" in str(e):
            # Remove the existing endpoint
            # Get ARN from error message
            endpoint_arn = str(e).split("EndpointArn: ")[1].split(" ")[0]
            sns.delete_endpoint(EndpointArn=endpoint_arn)
            # Create a new endpoint
            response = sns.create_platform_endpoint(
                PlatformApplicationArn=platform_application_arn,
                Token=device_id,
                CustomUserData=user_id
            )
            endpoint_arn = response['EndpointArn']
            # Update the user record in DynamoDB
            response = userTable.update_item(
                Key={'userId': user_id},
                UpdateExpression='SET notificationData.arn = :newArn, notificationData.deviceId = :newDeviceId',
                ExpressionAttributeValues={
                    ':newArn': endpoint_arn,
                    ':newDeviceId': device_id
                },
                ReturnValues='UPDATED_NEW'
            )
            return jsonify({"success": True, "message": "Device registered successfully", "data": response}), 200
        else:
            print(e)
            return jsonify({"success": False, "message": str(e)}), 500
    
@app.route('/signUp', methods=['POST'])
def sign_up():
    data = request.get_json()
    name = data.get('name')
    email = data.get('email')
    userId = data.get('userId')
    deviceId = data.get('deviceId')

    
    try:
        response = sns.create_platform_endpoint(
                PlatformApplicationArn=platform_application_arn,
                Token=deviceId,
                CustomUserData=userId
        )
        endpoint_arn = response['EndpointArn']
    except Exception as e:
        if "EndpointAlreadyExists" in str(e):
            # Remove the existing endpoint
            # Get ARN from error message
            endpoint_arn = str(e).split("EndpointArn: ")[1].split(" ")[0]
            sns.delete_endpoint(EndpointArn=endpoint_arn)
            # Create a new endpoint
            response = sns.create_platform_endpoint(
                PlatformApplicationArn=platform_application_arn,
                Token=deviceId,
                CustomUserData=userId
            )
            endpoint_arn = response['EndpointArn']
    # Add user to DynamoDB
    try:
        userTable.put_item(
            Item={
                'userId': userId,
                'name': name,
                'email': email,
                'notificationData': {
                    'arn': endpoint_arn,
                    'deviceId': deviceId
                }
            }
        )
        return jsonify({"success": True, "message": "User registered successfully"}), 200
    except Exception as e:
        print(e)
        return jsonify({"success": False, "message": str(e)}), 500

@app.route('/userData', methods=['POST'])
@firebase_auth_required
def get_user_data():
    data = request.get_json()
    user_id = data.get('userId')

    if not user_id:
        return jsonify({"success": False, "message": "User Id null"}), 400
    else:
        try:
            response = userTable.get_item(Key={'userId': user_id})
            if 'Item' not in response:
                return jsonify({"success": False, "message": "User not found"}), 404
            friends = response.get('Item', {}).get('friends', []) 
            # Get friends inffo from DynamoDB, but only userId, name and email
            friends_info = []
            for friend in friends:
                friend_response = userTable.get_item(Key={'userId': friend})
                if 'Item' not in friend_response:
                    continue
                friend_info = {
                    'userId': friend_response['Item']['userId'],
                    'name': friend_response['Item']['name'],
                    'email': friend_response['Item']['email']
                }
                friends_info.append(friend_info)
            userData = {}
            userData['userId'] = response['Item']['userId']
            userData['name'] = response['Item']['name']
            userData['email'] = response['Item']['email']
            userData['friends'] = friends_info

            return jsonify(userData), 200
            
        except Exception as e:
            print(e)
            return jsonify({"success": False, "message": str(e)}), 500


@app.route('/getPosts', methods=['POST'])
@firebase_auth_required
def get_posts():
    data = request.get_json()
    user_id = data.get('userId')
    friend_id = data.get('friendId')

    if not user_id:
        return jsonify({"success": False, "message": "User Id null"}), 400
    else:
        try:
            # Get posts where userId is `senderId` or `receiverId` and `friendId` is `senderId` or `receiverId`
            response = postsTable.scan(
                FilterExpression=(
                    (Attr('senderId').eq(user_id) | Attr('receiverId').eq(user_id)) &
                    (Attr('senderId').eq(friend_id) | Attr('receiverId').eq(friend_id))
                ),
                # ExpressionAttributeValues={
                #     ':userId': user_id,
                #     ':friendId': friend_id
                # }
            )
            posts = response['Items']
            return jsonify(posts), 200

        except Exception as e:
            print(e)
            return jsonify({"success": False, "message": str(e)}), 500

@app.route('/sendPost', methods=['POST'])
@firebase_auth_required
def send_post():
    if "image" not in request.files:
        return jsonify({"success": False, "message": "Image not found"}), 400
    
    userId = request.form.get('userId')
    friendId = request.form.get('friendId')

    imageData = request.files["image"]

    if not userId or not friendId:
        return jsonify({"success": False, "message": "User Id or Friend Id null"}), 400
    


    if not userId or not friendId:
        return jsonify({"success": False, "message": "User Id or Friend Id null"}), 400
    else:
        
        
        filename = secure_filename(imageData.filename)
        postId = str(uuid.uuid4())
        # s3.upload_fileobj(
        #     Fileobj=file,
        #     Bucket=bucket_name,
        #     Key=f"sharedImages/{postId}.jpg",
        # )
        
        try:
            s3.put_object(
            Bucket=bucket_name,
            Key=f"sharedImages/{postId}.jpg",
            Body=imageData.stream,
            ContentType=imageData.content_type,
            )
            image_url = f"https://{bucket_name}.s3.amazonaws.com/sharedImages/{postId}.jpg"
            # Add the post to DynamoDB
            postData = {
                    'postId': postId,
                    'senderId': userId,
                    'receiverId': friendId,
                    'image': image_url,
                    'dateTime': datetime.utcnow().strftime("%Y-%m-%d %H:%M"),
                }
            postsTable.put_item(
                Item=postData
            )
            return jsonify(postData), 200
        except Exception as e:
            print(e)
            return jsonify({"success": False, "message": str(e)}), 500

@app.route('/addFriend', methods=['POST'])
@firebase_auth_required
def add_friend():
    data = request.get_json()
    friend_email = data.get('friendEmail')
    user_id = data.get('userId')

    if not friend_email:
        return jsonify({"success": False, "message": "User Id or Friend Id null"}), 400
    else:
        try:
            # Add the friend to the user's friends list in DynamoDB
            response = userTable.scan(
                FilterExpression=Attr('email').eq(friend_email)
            )
            if 'Items' not in response or len(response['Items']) == 0:
                return jsonify({"success": False, "message": "Friend not found"}), 404
            else:
                friend_id = response['Items'][0]['userId']
                
                userTable.update_item(
                    Key={'userId': user_id},
                    UpdateExpression='ADD friends :newFriend',  
                    ExpressionAttributeValues={
                        ':newFriend': set([friend_id]),
                    },
                    ReturnValues='UPDATED_NEW'
                )

                # Add the user to the friend's friends list in DynamoDB
                userTable.update_item(
                    Key={'userId': friend_id},
                    UpdateExpression='ADD friends :newFriend',
                    ExpressionAttributeValues={
                        ':newFriend': set([user_id]),
                    },
                    ReturnValues='UPDATED_NEW'
                )

                return jsonify({"success": True, "message": "Friend added successfully"}), 200

        except Exception as e:
            print(e)
            return jsonify({"success": False, "message": str(e)}), 500


if __name__ == '__main__':
    app.run(host="0.0.0.0", port=6000)