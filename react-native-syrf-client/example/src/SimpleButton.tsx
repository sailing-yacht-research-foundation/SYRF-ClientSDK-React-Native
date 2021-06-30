import React from 'react';
import {
  TextStyle,
  TouchableOpacity,
  View,
  ViewStyle,
  Text,
  StyleSheet,
} from 'react-native';

type Props = {
  text: string;
  textStyle?: TextStyle;
  style?: ViewStyle;
  onPress?: () => void;
};

const SimpleButton: React.FC<Props> = ({ text, textStyle, style, onPress }) => (
  <TouchableOpacity style={[style, styles.container]} onPress={onPress}>
    <View>
      <Text style={textStyle}>{text}</Text>
    </View>
  </TouchableOpacity>
);

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    justifyContent: 'center',
    padding: 10,
    borderRadius: 4,
  },
});

export default SimpleButton;
