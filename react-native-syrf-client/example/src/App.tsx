import * as React from 'react';

import { StyleSheet, View, Text } from 'react-native';
import SyrfClient from 'react-native-syrf-client';

export default function App() {
  const [result, setResult] = React.useState<number | undefined>();

  React.useEffect(() => {
    SyrfClient.multiply(3, 7).then(setResult);
    SyrfClient.checkPermissions({}).then(setResult);
    SyrfClient.startLocationUpdates();
    SyrfClient.stopLocationUpdates();
  }, []);

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
